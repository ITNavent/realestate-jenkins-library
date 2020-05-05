def call(String statefulsetName, String releaseName, String vsName, String namespace) {
	def DOWN_COLOR = ""
	script {
		def UP_COLOR = getBlueGreenDeploymentColour(statefulsetName, vsName, namespace)
		if(UP_COLOR == "blue") {
			DOWN_COLOR = "green"
		} else if(UP_COLOR == "green") {
			DOWN_COLOR = "blue"
		} else {
			currentBuild.result = 'ABORTED'
			error("'${UP_COLOR}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		//Copio la cantidad de replicas actuales
		def DOWN_REPLICAS = sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-${UP_COLOR} -n ${namespace} -o jsonpath={.status.replicas}")
		sh """
		kubectl scale --replicas=${DOWN_REPLICAS} statefulset/${statefulsetName}-${UP_COLOR} -n ${namespace}
		kubectl wait-sts ${statefulsetName}-${UP_COLOR} -n ${namespace}
		"""
		updateVirtualServiceReuseValues(statefulsetName, releaseName, namespace, UP_COLOR)
	}
}