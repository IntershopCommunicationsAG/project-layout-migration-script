import com.intershop.gradle.test.AbstractIntegrationSpec
import org.gradle.api.*
import org.gradle.testfixtures.ProjectBuilder

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MigrationSpec extends AbstractIntegrationSpec {

    def setup() {
        prepareBuildFile()

        createJavaFile      "javasource", 'com.foo',       'HelloWorld'
        createJavaTestFile  "javasource", 'tests.com.foo', 'HelloWorldTest'

        createFile 'javasource/resources/foo.properties'      , 'foo=bar'
        createFile 'javasource/tests/resources/foo.properties', 'foo=bar'
    }

    def 'migration works'() {

        when: "gradle check publish"
        def resultPublishBefore = preparedGradleRunner
                .withGradleVersion('2.11')
                .withArguments('check', 'publish', '-is')
                .build()

        then: "publish finished successful"
        File beforeMigration = dumpProject "beforeMigration"
        resultPublishBefore.task(":check").outcome == SUCCESS
        resultPublishBefore.task(":jar").outcome == SUCCESS
        resultPublishBefore.task(":publish").outcome == SUCCESS

        when: "gradle migrate"
        def migrationScript = new File('migrate.gradle').absolutePath

        def resultMigrate = preparedGradleRunner
                .withGradleVersion('2.11')
                .withArguments('-PMOCK_SCM', 'clean', 'migrate', '-I', migrationScript, '-is',)
                .build()
        then: "migration finished sucessful"
        resultMigrate.task(":migrate").outcome == SUCCESS

        when: "gradle check publish"
        def resultPublishAfter = preparedGradleRunner
                .withGradleVersion('2.11')
                .withArguments('check', 'publish', '-is')
                .build()

        then: "publish finished successful"
        File afterMigration = dumpProject "afterMigration"
        resultPublishAfter.task(":check").outcome == SUCCESS
        resultPublishAfter.task(":publish").outcome == SUCCESS

        and: "compare related project folders"
        compareFileStructure beforeMigration, afterMigration, 'build/classes'
        compareFileStructure beforeMigration, afterMigration, 'build/resources'
        compareFileStructure beforeMigration, afterMigration, 'build/libs'
        compareFileStructure beforeMigration, afterMigration, 'build/publications'
        compareFileStructure beforeMigration, afterMigration, 'build/repo'
    }


    private def File prepareBuildFile() {
        buildFile << """
            buildscript {
                repositories {
                    jcenter()

                    ivy {
                        url "http://nexus/nexus/content/groups/gradle-all"
                        layout('pattern') {
                            ivy '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                            artifact '[organisation]/[module]/[revision]/[ext]s/[artifact]-[type](-[classifier])-[revision].[ext]'

                            ivy '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                            artifact '[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier])-[revision].[ext]'
                        }
                    }
                }

                dependencies {
                        classpath "com.intershop.build.gradle:ish-component-plugin:2.11.+"
                }
            }

            apply plugin : 'java-cartridge'
            apply plugin : 'static-cartridge'

            version = '42'
            group = 'foo'

            publishing {
                repositories {
                    ivy {
                        name "fileRepo"
                        url  'file://${testProjectDir.absolutePath.replace('\\', '/')}/build/repo'
                    }
                }
            }

            intershop {
                displayName = 'Dummy Cartridge'
            }

        """.stripIndent()
    }


    private File dumpProject(target='testProject') {
        File targetDir = new File("build/tmp/testProjects/$target")

        Project helperProject = ProjectBuilder.builder().build()

        if (targetDir.exists()) {
            helperProject.delete( targetDir.absolutePath )
        }

        helperProject.copy {
            from  testProjectDir.absolutePath
            into  targetDir.absolutePath
        }

        return targetDir
    }


    private File createFile(String fileName, String content='foo') {
        def pItems = fileName.split '/'

        def path = new File(testProjectDir, pItems.length == 1 ? '.' : pItems[0..(pItems.length - 2)].join('/'))

        def file = new File(path, pItems[pItems.length - 1])

        path.mkdirs()

        file << content

        return file
    }


    private File createJavaFile(String path, String dottedPackage, String className,

                 content = """package ${dottedPackage};

                    public class ${className} {

                        public static void main(String[] args) {
                            System.out.println("Hello Integration Test");
                        }
                    }
                 """.stripIndent())
    {
        return createFile("$path/${dottedPackage.replace('.', '/')}/${className}.java", content)
    }


    private File createJavaTestFile(String path, String dottedPackage, String className,

                 content = """package ${dottedPackage};

                    public class ${className} {

                        public static void main(String[] args) {
                            System.out.println("Hello Integration Test");
                        }
                    }
                 """.stripIndent())
    {
        return createJavaFile(path, dottedPackage, className, content)
    }

    private boolean compareFileStructure(File prj1, File prj2, String subFolder) {
        File before = new File(prj1, subFolder)
        File after =  new File(prj2, subFolder)

        if (!before.exists() && !after.exists()) return true

        if (before.isFile() && after.isFile()) {
            return before.size() == after.size()
        }

        if(before.isDirectory() && after.isDirectory()) {
            Set files = []

            files.add before.listFiles()*.name
            files.add after.listFiles()*.name

            return files.every() {
                compareFileStructure prj1, prj2, "subFolder/$it"
            }
        }

        println "compareFileStructure: $subFolder is different"

        return false
    }
}