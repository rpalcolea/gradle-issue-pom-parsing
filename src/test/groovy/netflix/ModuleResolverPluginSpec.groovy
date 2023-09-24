package netflix

import nebula.test.IntegrationTestKitSpec

import static org.gradle.testkit.runner.TaskOutcome.*

class ModuleResolverPluginSpec  extends IntegrationTestKitSpec {

    def "applies plugin"() {
        given:
        def repo = new File(getClass().getResource('/repo').toURI())
        buildFile << """
            buildscript {
                repositories {
                    maven {
                        url = '${repo}'
                    }
               }
            }
            plugins {
               id 'netflix.my-modules'
            }
            
            task helloWorld {
                doLast {
                    println 'Hello world!'
                }
            }
        """

        when:
        def result = runTasks('helloWorld')

        then:
        result.output.contains('Hello world!')
        result.task(":helloWorld").outcome == SUCCESS
    }
}
