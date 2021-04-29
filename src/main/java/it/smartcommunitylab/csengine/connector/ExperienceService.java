package it.smartcommunitylab.csengine.connector;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.smartcommunitylab.csengine.common.EntityType;
import it.smartcommunitylab.csengine.model.DataView;
import it.smartcommunitylab.csengine.model.Experience;
import it.smartcommunitylab.csengine.model.Person;
import it.smartcommunitylab.csengine.repository.ExperienceRepository;
import it.smartcommunitylab.csengine.repository.PersonRepository;
import it.smartcommunitylab.csengine.util.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ExperienceService {
	static Log logger = LogFactory.getLog(ExperienceService.class);
	
	@Autowired
	PersonRepository personRepository;
	@Autowired
	ExperienceRepository experienceRepository;
	@Autowired
	ConnectorManager connectorManager;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public Flux<Experience> refreshExam(String fiscalCode) {
		return personRepository.findByFiscalCode(fiscalCode)
				.switchIfEmpty(this.addNewPerson(fiscalCode))
				.flatMapMany(p -> this.mergeExperienceView(p, EntityType.exam.label));
	}

	public Flux<Experience> refreshStage(String fiscalCode) {
		return personRepository.findByFiscalCode(fiscalCode)
				.switchIfEmpty(this.addNewPerson(fiscalCode))
				.flatMapMany(p -> this.mergeExperienceView(p, EntityType.stage.label));
	}

	private Flux<Experience> mergeExperienceView(Person person, String entityType) {
		List<ConnectorConf> list = connectorManager.getExpConnectorsReverse(entityType);
		return Flux.fromIterable(list)
				.concatMap(conf -> {
					logger.info("mergeExperienceView:" + conf.getView());
					ExperienceConnector connector = connectorManager.getExpService(entityType, conf.getView());
					return connector.refreshExp(person).concatMap(e -> {
						DataView view = e.getViews().get(conf.getView());
						if(view != null) {
							return experienceRepository.findByExtRef(conf.getView(), 
									view.getIdentity().getExtUri(), view.getIdentity().getOrigin())
									.switchIfEmpty(this.findRelatedEntity(conf, e))
									.switchIfEmpty(experienceRepository.save(e))
									.flatMap(db -> this.updateExperience(db, e, conf.getView()));
						}
						return Mono.empty();
					});					
				}).thenMany(experienceRepository.findAllByPersonIdAndEntityType(person.getId(), entityType));
	}
	
	private Mono<Experience> findRelatedEntity(ConnectorConf conf, Experience e) {
		List<Map<String,String>> identityMap = connectorManager.getIdentityMap(conf.getEntityType());
		return Flux.fromIterable(identityMap)
				.filter(keyMap -> keyMap.containsKey(conf.getView()))
				.concatMap(keyMap -> {
					logger.info(String.format("findRelatedEntity:%s / %s", conf.getView(), keyMap));
					String extView = keyMap.keySet().stream().filter(key -> !key.equals(conf.getView())).findFirst().orElse(null);					
					if(Utils.isNotEmpty(extView)) {
						String extPath = "views." + extView + ".attributes." + keyMap.get(extView);
						Object value = e.getViews().get(conf.getView()).getAttributes().get(keyMap.get(conf.getView()));
						if(value == null) {
							return Mono.empty();
						}
						return experienceRepository.findByAttr(extPath, value);
					}
					return Mono.empty();
				}).next();
	}
	
	private Mono<Experience> updateExperience(Experience db, Experience e, String view) {
		logger.info("updateExperience:" + view);
		if(db.getId().equals(e.getId())) {
			return Mono.just(db);
		}
		if(db.getViews().get(EntityType.exp.label) != null) {
			db.getViews().get(EntityType.exp.label).getAttributes()
			.putAll(e.getViews().get(EntityType.exp.label).getAttributes());
		} else {
			db.getViews().put(EntityType.exp.label, e.getViews().get(EntityType.exp.label));
		}
		if(db.getViews().get(e.getEntityType()) != null) {
			db.getViews().get(e.getEntityType()).getAttributes()
			.putAll(e.getViews().get(e.getEntityType()).getAttributes());
		} else {
			db.getViews().put(e.getEntityType(), e.getViews().get(e.getEntityType()));
		}
		db.getViews().put(view, e.getViews().get(view));
		return experienceRepository.save(db);
	}
	
	private Mono<Person> addNewPerson(String fiscalCode) {
		Person p = new Person();
		p.setFiscalCode(fiscalCode);
		return personRepository.save(p);
	}
}
