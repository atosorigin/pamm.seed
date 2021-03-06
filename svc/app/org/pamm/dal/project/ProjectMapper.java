package org.pamm.dal.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.pamm.dal.RepositoryObjectFactory;
import org.pamm.domain.project.model.ProjectMember;
import org.pamm.domain.project.model.Project;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProjectMapper {

    private final RepositoryObjectFactory repositoryObjectFactory;

    @Inject
    public ProjectMapper(RepositoryObjectFactory repositoryObjectFactory) {
        this.repositoryObjectFactory = repositoryObjectFactory;
    }

    public ProjectEntity projectToEntity(final Project project) {
        final ProjectEntity projectEntity = new ProjectEntity();

        projectEntity.setId(project.getId());
        projectEntity.setTitle(project.getTitle());
        projectEntity.setProjectCode(project.getProjectCode());
        projectEntity.setClient(project.getClient());
        projectEntity.setSummary(project.getSummary());
        projectEntity.setStatus(project.getStatus());

        return projectEntity;
    }

    public ProjectUserEntity projectUserToEntity(final ProjectMember projectMember, final Project project) {
        final ProjectUserEntity projectUserEntity = new ProjectUserEntity();
        final ProjectUserId projectUserId = new ProjectUserId();

        projectUserId.setProjectId(project.getId());
        projectUserId.setUserEmail(projectMember.getEmail());

        projectUserEntity.setId(projectUserId);
        projectUserEntity.setUserId(projectMember.getUserId());
        projectUserEntity.setRole(projectMember.getRole().name());

        return projectUserEntity;
    }

    public List<ProjectUserEntity> projectUsersToEntityList(final Project project) {
        final List<ProjectUserEntity> projectUserEntities = new ArrayList<>();

        project.getMembers().forEach(projectMember -> projectUserEntities.add(projectUserToEntity(projectMember, project)));
        projectUserEntities.add(projectUserToEntity(project.getOwner(), project));

        return projectUserEntities;
    }

    public ProjectMember projectUserToBusinessObject(final ProjectUserEntity userEntity) {
        final ProjectMember member = new ProjectMember();
        member.setProjectId(userEntity.getId().projectId);
        member.setUserId(userEntity.getUserId());
        member.setEmail(userEntity.getId().getUserEmail());
        member.setRole(ProjectMember.Role.valueOf(userEntity.getRole()));

        // user may not have registered
        if (userEntity.getUser() != null) {
            member.setForename(userEntity.getUser().getForename());
            member.setSurname(userEntity.getUser().getSurname());
        }

        return member;
    }

    public List<Project> projectsToBusinessObjectList(final List<ProjectEntity> projectEntities) {
        final List<Project> projects = new ArrayList<>();
        projectEntities.forEach(projectEntity -> projects.add(projectToBusinessObject(projectEntity)));
        return projects;
    }

    public Project projectToBusinessObject(final ProjectEntity projectEntity) {
        final Project project = repositoryObjectFactory.createBusinessObject(projectEntity, Project.class);
        final List<ProjectMember> members = new ArrayList<>();
        for (ProjectUserEntity userEntity : projectEntity.getMembers()) {
            final ProjectMember member = projectUserToBusinessObject(userEntity);

            if (member.getRole().equals(ProjectMember.Role.OWNER)) {
                project.setOwner(member);
            } else {
                members.add(member);
            }
        }
        project.setMembers(members);
        return project;
    }
}
