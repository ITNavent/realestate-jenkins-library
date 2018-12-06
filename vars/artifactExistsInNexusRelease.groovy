def call(String artifactName, String artifactVersion, String packagingType) {
	def safePackagingType = packagingType? packagingType ?: 'jar'
	def statusCode = sh(script: "curl -sL -w '%{http_code}' -o /dev/null -I 'http://nexus.navent.biz:8081/nexus/content/repositories/releases/com/navent/realestate/${artifactName}/${artifactVersion}/${artifactName}-${artifactVersion}.${packagingType}'", returnStdout: true)
	return "200" == statusCode
}

