def call(String statefulsetName, String releaseName, String namespace, String colour, String chartLocation = './deploy/istio', String extraValues = "") {
	def DOWN_COLOR = ""
	def VS_MAP = ["blue": 0, "green": 0]
	def CURR_VS_MAP = ["blue": 0, "green": 0]
	def vsName = ""
	script {
		if(colour == "blue") {
			VS_MAP[colour] = 100
			DOWN_COLOR = "green"
		}
		else if(colour == "green") {
			VS_MAP[colour] = 100
			DOWN_COLOR = "blue"
		} else {
			currentBuild.result = 'ABORTED'
			error("'${colour}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
		}
		echo VS_MAP.toString()
	}
	retry(3) {
		sh """
		helm upgrade --install ${releaseName}-istio ${chartLocation} ${extraValues} \
		--namespace ${namespace} --set istio.blueWeight=${VS_MAP["blue"]} \
		--set istio.greenWeight=${VS_MAP["green"]} --set istio.blueGreen=true --atomic
		"""
		try {
			def ISTIO_YAML = sh(returnStdout: true, script: "helm get manifest ${releaseName}-istio -n ${namespace}")
			def ISTIO_ENTITIES = readYaml text: ISTIO_YAML
			for(istio_entity in ISTIO_ENTITIES) {
				if(istio_entity.kind == "VirtualService") {
					vsName = istio_entity.metadata.name
				}
			}
		} catch(err) {
			error("No se encontro el nombre del VirtualService en release ${releaseName}-istio")
		}
		try {
			def VS_JSON = sh(returnStdout: true, script: "kubectl get virtualservice ${vsName} -n ${namespace} -o json")
			def VS_PROPS = readJSON text: VS_JSON
			def VS_ROUTES = VS_PROPS.spec.http[0].route
			for(route in VS_ROUTES) {
				CURR_VS_MAP[route.destination.subset] = (route.weight ?: 0).toInteger()
			}
		} catch(err) {
		}
		echo "virtual service map en kubernetes ${CURR_VS_MAP.toString()}"
		if(CURR_VS_MAP[colour] != VS_MAP[colour]) {
			currentBuild.result = 'ABORTED'
			error("En release ${releaseName}-istio el peso del ${colour} en Virtual Service ${vsName} deberia ser ser ${VS_MAP[colour]} y es ${CURR_VS_MAP[colour]}")
		}
		echo "virtual service map ${VS_MAP.toString()}"
		try {
			(sh(script: "kubectl scale --replicas=0 statefulset/${statefulsetName}-${DOWN_COLOR} -n ${namespace}"))
		} catch(err) {}
	}
}
