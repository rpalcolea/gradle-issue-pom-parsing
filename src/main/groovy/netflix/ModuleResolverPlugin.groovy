package netflix

import groovy.transform.CompileDynamic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.BuildServiceSpec

class ModuleResolverPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def service = RepositoryContentDescriptorService.registerService(project).get()
        List<String> modulesWithGradleMetadata = service.gradleMetadataModules
        println modulesWithGradleMetadata
    }
}

abstract class RepositoryContentDescriptorService implements BuildService<Params> {
    interface Params extends BuildServiceParameters {
        Property<RepositoryContentDescriptors> getRepositoryContentDescriptors()
    }

    List<String> getGradleMetadataModules() {
        return getParameters().repositoryContentDescriptors.get().gradleMetadataModules
    }

    static Provider<RepositoryContentDescriptorService> registerService(Project project) {
        return project.gradle.sharedServices.registerIfAbsent('repositoryContentFilters', RepositoryContentDescriptorService.class, { BuildServiceSpec<Params> spec ->
            RepositoryContentDescriptors repositoryContentFilters = resolveRepositoryContentDescriptors(project)
            spec.parameters.repositoryContentDescriptors.set(repositoryContentFilters)
        })
    }

    @CompileDynamic
    static RepositoryContentDescriptors resolveRepositoryContentDescriptors(Project project) {
        List<String> gradleMetadataModules = resolveDescriptors(project,
                'my-modules',
                'myModulesConfiguration')
        return new RepositoryContentDescriptors(gradleMetadataModules)
    }

    private static List<String> resolveDescriptors(Project project, String module, String configName) {
         Configuration configuration = project.buildscript.configurations.maybeCreate(configName)
        configuration.transitive = false
        configuration.withDependencies { dependencies ->
            dependencies.add(project.dependencies.create("netflix:$module:latest.release"))
        }
        File artifactFile = configuration.resolvedConfiguration.resolvedArtifacts
                .first()
                .file
        return ["something"]
    }

    static class RepositoryContentDescriptors implements Serializable {
        final List<String> gradleMetadataModules

        RepositoryContentDescriptors(List<String> gradleMetadataModules) {
            this.gradleMetadataModules = gradleMetadataModules
        }
    }
}
