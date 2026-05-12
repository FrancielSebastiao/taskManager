package com.fransebastiao.taskmanager;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.domain.project.ProjectMemberRole;
import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.domain.user.Privilege;
import com.fransebastiao.taskmanager.domain.user.Role;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.repository.PrivilegeRepository;
import com.fransebastiao.taskmanager.repository.ProjectCategoryRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRoleRepository;
import com.fransebastiao.taskmanager.repository.RoleRepository;
import com.fransebastiao.taskmanager.repository.TaskCategoryRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectMemberRoleRepository memberRoleRepository;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // == create initial privileges ===== PROJECTS
        final Privilege readProjectsPrivilege = createPrivilegeIfNotFound("LER_PROJECTOS");
        final Privilege createProjectsPrivilege = createPrivilegeIfNotFound("CRIAR_PROJECTOS");
        final Privilege deleteProjectsPrivilege = createPrivilegeIfNotFound("APAGAR_PROJECTOS");
        final Privilege updateProjectsPrivilege = createPrivilegeIfNotFound("EDITAR_PROJECTOS");

        // ============ PASSWORD
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("MUDAR_PASSWORD");
        
        // ============ TASKS
        final Privilege createTasksPrivilege = createPrivilegeIfNotFound("CRIAR_TAREFAS");
        final Privilege updateTasksPrivilege = createPrivilegeIfNotFound("EDITAR_TAREFAS");
        final Privilege readTasksPrivilege = createPrivilegeIfNotFound("LER_TAREFAS");
        final Privilege deleteTasksPrivilege = createPrivilegeIfNotFound("APAGAR_TAREFAS");

        // ============ PROGRESS & COMMENTS
        final Privilege updateProgressPrivilege = createPrivilegeIfNotFound("EDITAR_PROGRESSO");
        final Privilege createCommentsPrivilege = createPrivilegeIfNotFound("CRIAR_COMENTARIOS");
        final Privilege deleteCommentsPrivilege = createPrivilegeIfNotFound("APAGAR_COMENTARIOS");
        final Privilege updateCommentsPrivilege = createPrivilegeIfNotFound("EDITAR_COMENTARIOS");

        // ============ MATERIALS & LABOUR
        final Privilege createMaterialsPrivilege = createPrivilegeIfNotFound("CRIAR_MATERIAIS");
        final Privilege updateMaterialsPrivilege = createPrivilegeIfNotFound("EDITAR_MATERIAIS");
        final Privilege deleteMaterialsPrivilege = createPrivilegeIfNotFound("APAGAR_MATERIAIS");
        final Privilege readMaterialsPrivilege = createPrivilegeIfNotFound("LER_MATERIAIS");
        final Privilege registerMaterialUsagePrivilege = createPrivilegeIfNotFound("REGISTAR_USO_DE_MATERIAL"); 
        final Privilege createLaborEntryPrivilege = createPrivilegeIfNotFound("CRIAR_REGISTRO_DE_PRODUCAO");
        final Privilege completeLaborEntryPrivilege = createPrivilegeIfNotFound("COMPLETAR_REGISTRO_DE_PRODUCAO");

        // ============ REPORTS
        final Privilege readReportsPrivilege = createPrivilegeIfNotFound("LER_RELATORIOS");
        final Privilege readCostsPrivilege = createPrivilegeIfNotFound("LER_CUSTOS");

        // ============ PHOTOS
        final Privilege uploadPhotosPrivilege = createPrivilegeIfNotFound("CARREGAMENTO_DE_IMAGENS");
        final Privilege deletePhotosPrivilege = createPrivilegeIfNotFound("APAGAR_IMAGENS");
    
        // ============ USERS
        final Privilege readUsersPrivilege = createPrivilegeIfNotFound("LER_USUARIOS");

        // == create initial roles
        final Set<Privilege> sysAdminPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, createProjectsPrivilege, deleteProjectsPrivilege, updateProjectsPrivilege, passwordPrivilege, createTasksPrivilege, updateTasksPrivilege, readTasksPrivilege, deleteTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, createMaterialsPrivilege, updateMaterialsPrivilege, deleteMaterialsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, readCostsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege, readUsersPrivilege));
        final Set<Privilege> adminPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, createProjectsPrivilege, deleteProjectsPrivilege, updateProjectsPrivilege, passwordPrivilege, createTasksPrivilege, updateTasksPrivilege, readTasksPrivilege, deleteTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, createMaterialsPrivilege, updateMaterialsPrivilege, deleteMaterialsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, readCostsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege, readUsersPrivilege));
        final Set<Privilege> managerPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, createProjectsPrivilege, deleteProjectsPrivilege, updateProjectsPrivilege, passwordPrivilege, createTasksPrivilege, updateTasksPrivilege, readTasksPrivilege, deleteTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, createMaterialsPrivilege, updateMaterialsPrivilege, deleteMaterialsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, readCostsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege));
        final Set<Privilege> supervisorPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, passwordPrivilege, createTasksPrivilege, updateTasksPrivilege, readTasksPrivilege, deleteTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, createMaterialsPrivilege, updateMaterialsPrivilege, deleteMaterialsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, readCostsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege));
        final Set<Privilege> engineerPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, passwordPrivilege, createTasksPrivilege, updateTasksPrivilege, readTasksPrivilege, deleteTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, createMaterialsPrivilege, updateMaterialsPrivilege, deleteMaterialsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, readCostsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege));
        final Set<Privilege> workerPrivileges = new HashSet<>(Set.of(readProjectsPrivilege, passwordPrivilege, readTasksPrivilege, updateProgressPrivilege, createCommentsPrivilege, deleteCommentsPrivilege, updateCommentsPrivilege, readMaterialsPrivilege, registerMaterialUsagePrivilege, createLaborEntryPrivilege, completeLaborEntryPrivilege, readReportsPrivilege, uploadPhotosPrivilege, deletePhotosPrivilege));
        
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        final Role sysAdminRole = createRoleIfNotFound("ROLE_SYSADMIN", sysAdminPrivileges);
        final Role managerRole = createRoleIfNotFound("ROLE_GESTOR", managerPrivileges);
        final Role supervisorRole = createRoleIfNotFound("ROLE_SUPERVISOR", supervisorPrivileges);
        final Role engineerRole = createRoleIfNotFound("ROLE_ENGENHEIRO", engineerPrivileges);
        final Role workerRole = createRoleIfNotFound("ROLE_TRABALHADOR", workerPrivileges);

        // CREATE TASK CATEGORIES
        createTaskCategoryIfNotFound("Estrutural",    "Trabalhos de estrutura e fundações");
        createTaskCategoryIfNotFound("Hidráulica",    "Instalações hidráulicas e drenagem");
        createTaskCategoryIfNotFound("Elétrica",      "Instalações elétricas e painéis");
        createTaskCategoryIfNotFound("Acabamentos",   "Revestimentos, pinturas e acabamentos");
        createTaskCategoryIfNotFound("Inspeção",      "Vistorias e controlo de qualidade");
        createTaskCategoryIfNotFound("Terraplanagem", "Movimentação de terras e nivelamento");
        createTaskCategoryIfNotFound("Outro",         "Outras atividades não categorizadas");

        // CREATE PROJECT CATEGORIES
        createProjectCategoryIfNotFound("Construção Nova",    "Execução de obras de raiz, incluindo edifícios residenciais, comerciais, industriais e de utilidade pública");
        createProjectCategoryIfNotFound("Reabilitação e Remodelação",    "Intervenções em estruturas existentes visando a recuperção, modernização ou adaptação funcional");
        createProjectCategoryIfNotFound("Infraestruturas Urbanas",      "Construção e manutenção de arruamentos, redes de saneamento, abastecimento de água e drenagem");
        createProjectCategoryIfNotFound("Fiscalização de Obras",   "Acompanhamento e controlo técnico de empreitadas garantindo conformidade com projecto, prazo e orçamento");
        createProjectCategoryIfNotFound("Auditoria Técnica",      "Avaliação independente da qualidade, segurança e conformidade de obras e instalações");
        createProjectCategoryIfNotFound("Consultoria e Projecto", "Elaboração de estudos, projectos de arquitectura e engenharia, e pareceres técnicos especializados");
        createProjectCategoryIfNotFound("Outro",         "Outras atividades não categorizadas");



        // == create initial user
        createUserIfNotFound("sysadmin@test.com", "Sys Admin", "P4ssword#", new HashSet<>(Set.of(sysAdminRole))); 
        createUserIfNotFound("admin@test.com", "Admin", "P4ssword#", new HashSet<>(Set.of(adminRole)));
        createUserIfNotFound("manager@test.com", "Manager", "P4ssword#", new HashSet<>(Set.of(managerRole)));
        createUserIfNotFound("supervisor@test.com", "Supervisor", "P4ssword#", new HashSet<>(Set.of(supervisorRole)));
        createUserIfNotFound("engineer@test.com", "Engineer", "P4ssword#", new HashSet<>(Set.of(engineerRole)));
        createUserIfNotFound("worker@test.com", "Worker", "P4ssword#", new HashSet<>(Set.of(workerRole)));

        createMemberRoleIfNotFound("GESTOR", "Lidera o projecto, define objectivos e aloca recursos");
        createMemberRoleIfNotFound("SUPERVISOR", "Acompanha o progresso, garante que as tarefas sejam feitas e reporta ao gestor");
        createMemberRoleIfNotFound("ENGENHEIRO", "Desenvolve soluções técnicas e trabalha nas tarefas especificas do projecto");
        createMemberRoleIfNotFound("ARQUITECTO", "Define a estrutura e design do projecto, garantindo que atenda aos requisitos técnicos e funcionais");
        createMemberRoleIfNotFound("TRABALHADOR", "Executa tarefas operacionais e contribui para o sucesso do projecto");


        alreadySetup = true;
    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(final String name) {
        return privilegeRepository.findByName(name).orElseGet(() -> {
            Privilege newPrivilege = new Privilege(name);
            newPrivilege = privilegeRepository.save(newPrivilege);
            return newPrivilege;
        });
    }

    @Transactional
    public Role createRoleIfNotFound(final String name, final Set<Privilege> privileges) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role role = new Role(name);
            role.setPrivileges(privileges);
            role = roleRepository.save(role);
            return role;
        }); 
    }

    @Transactional
    public User createUserIfNotFound(final String email, final String name, final String password, final Set<Role> roles) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(
                name,
                email,                
                passwordEncoder.encode(password)
            );
            user.setActive(true);
            user.setRoles(roles);
            return userRepository.save(user);
        });
    }

    @Transactional
    public void createTaskCategoryIfNotFound(String name, String description) {
        if (!taskCategoryRepository.existsByName(name)) {
            taskCategoryRepository.save(new TaskCategory(name, description));
        }
    }

    @Transactional
    public void createProjectCategoryIfNotFound(String name, String description) {
        if (!projectCategoryRepository.existsByName(name)) {
            projectCategoryRepository.save(new ProjectCategory(name, description));
        }
    }

    @Transactional 
    public void createMemberRoleIfNotFound(String name, String description) {
        if(!memberRoleRepository.existsByNameIgnoreCase(name)) {
            memberRoleRepository.save(new ProjectMemberRole(name, description));
        }
    }
}