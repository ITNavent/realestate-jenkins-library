def call(String statefulsetName, String releaseName, String namespace, String colour, String chartLocation = './deploy/istio', String extraValues = "") {
	def DOWN_COLOR = ""
	def VS_MAP = ["blue": 0, "green": 0, "red": 0]
	def CURR_VS_MAP = ["blue": 0, "green": 0, "red": 0]
	def vsName = ""
	def UP_COLOUR = ""
	script {
		vsName = getVirualServiceName(releaseName, namespace)
		def upColourPct = 100
		if(colour == "red") {
			//Estoy activando red.
			UP_COLOUR = currentBlueGreenDeploymentColour(statefulsetName, vsName, namespace)
		} else {
			//Estoy activando blue o green.
			UP_COLOUR = colour		
		}

		def RED_ACTIVE = isColourActive(vsName, "red", namespace)
		if(RED_ACTIVE || (colour == "red")) {
			upColourPct = 50
			VS_MAP["red"] = upColourPct
		}

		if(UP_COLOUR == "blue") {
			VS_MAP[UP_COLOUR] = upColourPct
			DOWN_COLOR = "green"
		} else if(UP_COLOUR == "green") {
			VS_MAP[UP_COLOUR] = upColourPct
			DOWN_COLOR = "blue"
		} else {
			currentBuild.result = 'ABORTED'
			error("'${colour}' no es un color de deploy valido, debe ser uno de ['blue', 'green', 'red']")
		}		      
		echo VS_MAP.toString()
	}
	retry(3) {
		sh """
		helm upgrade --install ${releaseName}-istio ${chartLocation} ${extraValues} \
		--namespace ${namespace} --set istio.blueWeight=${VS_MAP["blue"]} \
		--set istio.greenWeight=${VS_MAP["green"]} --set istio.redWeight=${VS_MAP["red"]} \
		--set istio.blueGreen=true --atomic
		"""
		try {
			def VS_JSON = sh(returnStdout: true, script: "kubectl get virtualservice ${vsName} -n ${namespace} -o json")
			def VS_PROPS = readJSON text: VS_JSON
			def VS_ROUTES = VS_PROPS.spec.http[0].route
			for(route in VS_ROUTES) {
				CURR_VS_MAP[route.destination.subset] = (route.weight ?: 0).toInteger()
			}
			VS_ROUTES = null
		} catch(err) {
		}
		echo "virtual service map esperado ${VS_MAP.toString()}"
		echo "virtual service map en kubernetes ${CURR_VS_MAP.toString()}"
		if(CURR_VS_MAP[UP_COLOUR] != VS_MAP[UP_COLOUR]) {
			currentBuild.result = 'ABORTED'
			error("En release ${releaseName}-istio el peso del ${UP_COLOUR} en Virtual Service ${vsName} deberia ser ser ${VS_MAP[UP_COLOUR]} y es ${CURR_VS_MAP[UP_COLOUR]}")
		}
		try {
			(sh(script: "kubectl scale --replicas=0 statefulset/${statefulsetName}-${DOWN_COLOR} -n ${namespace}"))
		} catch(err) {}
	}
}
