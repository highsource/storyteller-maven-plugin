call mvn storyteller:display-dependency-graph -DgroupId=org.hibernate -DartifactId=hibernate-core -Dversion=3.3.2.GA
call mvn storyteller:export-dependency-graph -Dfile=hibernate-core.png -DgroupId=org.hibernate -DartifactId=hibernate-core -Dversion=3.3.2.GA -DrepositoryURL=http://repository.jboss.com/maven2
call mvn storyteller:export-dependency-graph -Dfile=hibernate-entitymanager.png -DgroupId=org.hibernate -DartifactId=hibernate-entitymanager -Dversion=3.4.0.GA -DrepositoryURL=http://repository.jboss.com/maven2
call mvn storyteller:analyze-dependency-graph -DgroupId=org.hibernate -DartifactId=hibernate-entitymanager -Dversion=3.4.0.GA -DrepositoryURL=http://repository.jboss.com/maven2