def call(String artifactName, String artifactVersion, String packagingType = 'jar') {
	sh(script: "curl -o ${artifactName}-${artifactVersion}.${packagingType} 'http://nexus.navent.biz:8081/nexus/content/repositories/releases/com/navent/realestate/${artifactName}/${artifactVersion}/${artifactName}-${artifactVersion}.${packagingType}'", returnStdout: false)
}

