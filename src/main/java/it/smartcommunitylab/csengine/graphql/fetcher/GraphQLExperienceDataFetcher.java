package it.smartcommunitylab.csengine.graphql.fetcher;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import it.smartcommunitylab.csengine.common.CertificationAttr;
import it.smartcommunitylab.csengine.common.CompetenceAttr;
import it.smartcommunitylab.csengine.common.EducatinalActivityAttr;
import it.smartcommunitylab.csengine.common.EducationAttr;
import it.smartcommunitylab.csengine.common.EntityType;
import it.smartcommunitylab.csengine.common.ExpAttr;
import it.smartcommunitylab.csengine.common.OrganisationAttr;
import it.smartcommunitylab.csengine.model.Address;
import it.smartcommunitylab.csengine.model.DataView;
import it.smartcommunitylab.csengine.model.Experience;
import it.smartcommunitylab.csengine.model.GeoPoint;
import it.smartcommunitylab.csengine.model.dto.CertificationDTO;
import it.smartcommunitylab.csengine.model.dto.CompetenceDTO;
import it.smartcommunitylab.csengine.model.dto.EnrollmentDTO;
import it.smartcommunitylab.csengine.model.dto.ExamDTO;
import it.smartcommunitylab.csengine.model.dto.ExperienceDTO;
import it.smartcommunitylab.csengine.model.dto.MobilityDTO;
import it.smartcommunitylab.csengine.model.dto.OrganisationDTO;
import it.smartcommunitylab.csengine.model.dto.StageDTO;
import it.smartcommunitylab.csengine.repository.ExperienceRepository;
import it.smartcommunitylab.csengine.util.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GraphQLExperienceDataFetcher {
	@Autowired
	ExperienceRepository experienceRepository;
	
	public DataFetcher<Stream<ExperienceDTO>> searchExpByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonId(personId)
					.map(this::getExpDTO)
					.toStream();
		};
  }

	public DataFetcher<Stream<ExamDTO>> searchExamByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonIdAndEntityType(personId, EntityType.educationalActivity.label)
					.map(this::getExamDTO)
					.toStream();
		};
	}

  public DataFetcher<Stream<StageDTO>> searchStageByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonIdAndEntityType(personId, EntityType.educationalActivity.label)
					.map(this::getStageDTO)
					.toStream();
		};
  }

	public DataFetcher<Stream<CertificationDTO>> searchCertificationByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonIdAndEntityType(personId, EntityType.certification.label)
					.map(this::getCertificationDTO)
					.toStream();
		};
  }

	public DataFetcher<Stream<MobilityDTO>> searchMobilityByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonIdAndEntityType(personId, EntityType.educationalActivity.label)
					.map(this::getMobilityDTO)
					.toStream();
		};
  }

	public DataFetcher<Stream<EnrollmentDTO>> searchEnrollmentByPersonId() {
		return dataFetchingEnvironment -> {
			String personId = dataFetchingEnvironment.getArgument("personId");
			return experienceRepository.findAllByPersonIdAndEntityType(personId, EntityType.education.label)
					.map(this::getEnrollmentDTO)
					.toStream();
		};
  }

	public DataFetcher<OrganisationDTO> getOrganisation() {
		return dataFetchingEnvironment -> {
			ExperienceDTO exp = dataFetchingEnvironment.getSource();
			return experienceRepository.findById(exp.getId()).flatMap(this::getOrganisationDTO).block();
		};
	}
	
	public DataFetcher<Address> getOrganisationAddress() {
		return dataFetchingEnvironment -> {
			OrganisationDTO org = dataFetchingEnvironment.getSource();
			return org.getAddress();
		};
	}

	public DataFetcher<Stream<CompetenceDTO>> getCompetences() {
		return dataFetchingEnvironment -> {
			ExperienceDTO exp = dataFetchingEnvironment.getSource();
			return experienceRepository.findById(exp.getId()).flatMapMany(this::getCompetenceDTO).toStream();
		};
	}

	public DataFetcher<Address> getAddress(String attr) {
		return dataFetchingEnvironment -> {
			Object source = dataFetchingEnvironment.getSource();
			try {
				PropertyDescriptor pd = new PropertyDescriptor(attr, source.getClass());
				if(pd != null) {
					return (Address) pd.getReadMethod().invoke(source);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	public DataFetcher<GeoPoint> getGeoPoint(String attr) {
		return dataFetchingEnvironment -> {
			Object source = dataFetchingEnvironment.getSource();
			try {
				PropertyDescriptor pd = new PropertyDescriptor(attr, source.getClass());
				if(pd != null) {
					return (GeoPoint) pd.getReadMethod().invoke(source);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		};
	}

	private void fillExpDTO(ExperienceDTO dto, Experience e) {
		DataView expView = e.getViews().get(EntityType.exp.label);
		if(expView != null) {
			dto.setId(e.getId());
			dto.setEntityType(e.getEntityType());
			dto.setPersonId(e.getPersonId());
			dto.setTitle((String) expView.getAttributes().get(ExpAttr.title.label));
			dto.setDescription((String) expView.getAttributes().get(ExpAttr.description.label));
			dto.setLocation((GeoPoint) expView.getAttributes().get(ExpAttr.location.label));
			dto.setDateFrom((String) expView.getAttributes().get(ExpAttr.dateFrom.label));
			dto.setDateTo((String) expView.getAttributes().get(ExpAttr.dateTo.label));
			dto.setValidityFrom((String) expView.getAttributes().get(ExpAttr.validityFrom.label));
			dto.setValidityTo((String) expView.getAttributes().get(ExpAttr.validityTo.label));
		}
	} 

	private ExperienceDTO getExpDTO(Experience e) {
		ExperienceDTO dto = new ExperienceDTO();
		fillExpDTO(dto, e);
		return dto;
	}

	private ExamDTO getExamDTO(Experience e) {
		ExamDTO dto = new ExamDTO();
		fillExpDTO(dto, e);
		DataView examView = e.getViews().get(EntityType.education.label);	
		if(examView != null) {
			dto.setType((String) examView.getAttributes().get(EducationAttr.type.label));
			dto.setQualification((String) examView.getAttributes().get(EducationAttr.qualification.label));
			dto.setHonour((Boolean) examView.getAttributes().get(EducationAttr.honour.label));
			dto.setGrade((String) examView.getAttributes().get(EducationAttr.grade.label));
			//dto.setResult((Boolean) examView.getAttributes().get(EducationAttr.result.label));
			//dto.setExternalCandidate((Boolean) examView.getAttributes().get(EducationAttr.externalCandidate.label));			
		}
		return dto;
	}

	private StageDTO getStageDTO(Experience e) {
		StageDTO dto = new StageDTO();
		fillExpDTO(dto, e);
		DataView view = e.getViews().get(EntityType.educationalActivity.label);
		if(view != null) {
			dto.setType((String) view.getAttributes().get(EducatinalActivityAttr.type.label));
			dto.setDuration((String) view.getAttributes().get(EducatinalActivityAttr.duration.label));
			//dto.setContact((String) view.getAttributes().get(EducatinalActivityAttr.contact.label));
			dto.setAddress((Address) view.getAttributes().get(EducatinalActivityAttr.address.label));
		}
		return dto;
	}

	private CertificationDTO getCertificationDTO(Experience e) {
		CertificationDTO dto = new CertificationDTO();
		fillExpDTO(dto, e);
		DataView view = e.getViews().get(EntityType.certification.label);
		if(view != null) {
			dto.setType((String) view.getAttributes().get(CertificationAttr.type.label));
			dto.setDuration((String) view.getAttributes().get(CertificationAttr.duration.label));
			dto.setAddress((Address) view.getAttributes().get(CertificationAttr.address.label));
			//dto.setContact((String) view.getAttributes().get(CertificationAttr.contact.label));
			dto.setGrade((String) view.getAttributes().get(CertificationAttr.grade.label));
			dto.setLanguage((String) view.getAttributes().get(CertificationAttr.language.label));
			dto.setLevel((String) view.getAttributes().get(CertificationAttr.level.label));
		}
		return dto;
	}

	private MobilityDTO getMobilityDTO(Experience e) {
		//TODO getMobilityDTO
		MobilityDTO dto = new MobilityDTO();
		fillExpDTO(dto, e);
		DataView view = e.getViews().get(EntityType.certification.label);
		if(view != null) {

		}
		return dto;
	}

	private EnrollmentDTO getEnrollmentDTO(Experience e) {
		EnrollmentDTO dto = new EnrollmentDTO();
		fillExpDTO(dto, e);
		DataView view = e.getViews().get(EntityType.education.label);
		if(view != null) {
			//dto.setSchoolYear((String) view.getAttributes().get(EducatinalActivityAttr.schoolYear.label));
			//dto.setCourse((String) view.getAttributes().get(EducatinalActivityAttr.course.label));
			//dto.setClassroom((String) view.getAttributes().get(EducatinalActivityAttr.classroom.label));
		}
		return dto;
	}
	
	@SuppressWarnings("unchecked")
	private Mono<OrganisationDTO> getOrganisationDTO(Experience e) {
		if(e.getViews().containsKey(EntityType.exp.label)) {
			DataView view = e.getViews().get(EntityType.exp.label);
			if(view.getAttributes().containsKey(ExpAttr.organisation.label)) {
				Map<String, Object> organisationView = (Map<String, Object>) view.getAttributes().get(ExpAttr.organisation.label);
				return Mono.just(getOrganisationDTO(organisationView));
			}
		}
		return Mono.empty();
	}
	
	private OrganisationDTO getOrganisationDTO(Map<String, Object> view) {
		OrganisationDTO dto = new OrganisationDTO();
		dto.setFiscalCode((String) view.get(OrganisationAttr.fiscalCode.label));
		dto.setName((String) view.get(OrganisationAttr.name.label));
		dto.setDescription((String) view.get(OrganisationAttr.description.label));
		dto.setAddress((Address) view.get(OrganisationAttr.address.label));
		dto.setLocation((GeoPoint) view.get(OrganisationAttr.location.label));
		dto.setPhone((String) view.get(OrganisationAttr.phone.label));
		dto.setEmail((String) view.get(OrganisationAttr.email.label));
		dto.setPec((String) view.get(OrganisationAttr.pec.label));
		return dto;
	}
	
	@SuppressWarnings("unchecked")
	private Flux<CompetenceDTO> getCompetenceDTO(Experience e) {
		List<CompetenceDTO> list = new ArrayList<>();
		DataView view = e.getViews().get(EntityType.exp.label);
		if(view.getAttributes().containsKey(ExpAttr.competences.label)) {
			List<Map<String, Object>> compList = (List<Map<String, Object>>) view.getAttributes().get(ExpAttr.competences.label);
			compList.forEach(map -> list.add(getCompetenceDTO(map, "it")));
		}
		return Flux.fromIterable(list);
	}
	
	@SuppressWarnings("unchecked")
	private CompetenceDTO getCompetenceDTO(Map<String, Object> map, String lang) {
		CompetenceDTO dto = new CompetenceDTO();
		dto.setUri((String) map.get(CompetenceAttr.uri.label));
		dto.setConcentType((String) map.get(CompetenceAttr.concentType.label));
		dto.setPreferredLabel(Utils.getLabel((Map<String, String>) map.get(CompetenceAttr.preferredLabel.label), lang));
		dto.setAltLabel(Utils.getLabel((Map<String, String>) map.get(CompetenceAttr.altLabel.label), lang));
		dto.setDescription(Utils.getLabel((Map<String, String>) map.get(CompetenceAttr.description.label), lang));
		return dto;
	}

}
