@Echo on
set JAVA_HOME=C:\Java\jdk1.6.0_45
set MAVEN_HOME=C:\apacheMaven\apache-maven-3.2.1
set MAVEN_OPTS=-Xms128m -Xmx512m
set CLASSPATH=.;
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin

@Echo off
Echo.

setlocal EnableDelayedExpansion

if "%1"=="" GOTO Usage

if "%1"=="in-memory" (
     echo Starting Schema Repo Server with in In-Memory backend
     java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/in-memory-config.properties
) else (
        @if "%1"=="file-system" (
             echo Starting Schema Repo Server with local file system backend
             java -cp bundle/target/schema-repo-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer bundle/config/local-file-system-config.properties
        ) else (
                  @if "%1"=="zookeeper" (
                        echo Starting Schema Repo Server with ZooKeeper backend
                        java -cp zk-bundle/target/schema-repo-zk-bundle-*-withdeps.jar org.schemarepo.server.RepositoryServer zk-bundle/config/config.properties
                  ) 
        )
)

goto:eof


:Usage
Echo Usage: %0% ^<backend-type^>
Echo.
Echo backend-type : Specify which backend to run. Valid values are: in-memory, file-system and zookeeper.
goto:eof
