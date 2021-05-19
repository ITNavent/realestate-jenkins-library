def call(String statefulsetName, String namespace) {
	def VERSION = ""
	script {
        def IMAGE = (sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName} -n ${namespace} -o jsonpath={.spec.template.spec.containers[0].image}")).toInteger()
    	VERSION = IMAGE.split(':')[1]
    	echo "Statefulset ${statefulsetName} image ${IMAGE} version ${VERSION}"
        return VERSION
    }
}