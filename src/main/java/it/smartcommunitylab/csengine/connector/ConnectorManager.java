package it.smartcommunitylab.csengine.connector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.smartcommunitylab.csengine.common.EntityType;

@Component
public class ConnectorManager {
	@Autowired
	private ApplicationContext context;
	
	List<ConnectorConf> servicesConfs;
	Map<String, ExperienceConnector> experienceMap = new HashMap<>();
	Map<String, PersonConnector> personMap = new HashMap<>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostConstruct
	public void initServices() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource("classpath:connectors.json");
		servicesConfs = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<ConnectorConf>>(){});
		for(ConnectorConf conf : servicesConfs) {
			try {
				Class c = Class.forName(conf.getImplementor());
				if(EntityType.person.label.equals(conf.getEntityType())) {
					PersonConnector connector = (PersonConnector) context.getBean(c);
					connector.setView(conf.getView());
					personMap.put(conf.getView(), connector);
				} else {
					ExperienceConnector connector = (ExperienceConnector) context.getBean(c);
					connector.setView(conf.getView());
					String serviceKey = getServiceKey(conf.getEntityType(), conf.getView());
					experienceMap.put(serviceKey, connector);					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	private String getServiceKey(String entityType, String view) {
		return entityType + "_" + view;
	}

	public PersonConnector getPersonService(String view) {
		return personMap.get(view);
	}
	
	public ExperienceConnector getExpService(String entityType, String view) {
		return experienceMap.get(getServiceKey(entityType, view));
	}
	
	public ConnectorConf getExpConnector(String entityType, int priority) {
		for(ConnectorConf conf : servicesConfs) {
			if(conf.getEntityType().equals(entityType) && (conf.getPriority() == priority)) {
				return conf;
			}
		}
		return null;
	}
	
	public List<ConnectorConf> getExpConnectorsReverse(String entityType) {
		List<ConnectorConf> list = servicesConfs.stream()
				.filter(conf -> conf.getEntityType().equals(entityType))
				.sorted(new Comparator<ConnectorConf>() {
			    @Override
			    public int compare(ConnectorConf o1, ConnectorConf o2) {
			    		if (o1.getPriority() == o2.getPriority())
			    			return 0;
			        return o1.getPriority() > o2.getPriority() ? -1 : 1;
			    }
				})
				.collect(Collectors.toList());     
		return list;
	}
	
}
