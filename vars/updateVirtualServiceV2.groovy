def call(String statefulsetName, String releaseName, String vsName, String namespace, List<String> colors, String chartLocation = './deploy/istio', String extraValues = "") {
	def DOWN_COLOR = ""
	def VS_MAP = ["blue": 0, "green": 0]
	def CURR_VS_MAP = ["blue": 0, "green": 0]
	script {
		if(colors.size == 2) {
			VS_MAP["blue"] = 50
			VS_MAP["green"] = 50
		} else if(colors.size == 1) {
     		if(colors[0] == "blue") {
				VS_MAP[colors[0]] = 100
				DOWN_COLOR = "green"
			} else if(colors[0] == "green") {
				VS_MAP[colors[0]] = 100
				DOWN_COLOR = "blue"
			} else {
				currentBuild.result = 'ABORTED'
				error("'${color[0]}' no es un color de deploy valido, debe ser uno de ['blue', 'green']")
			}
		} else {
			error("Debe indicar 1 o 2 colores de ['blue', 'green']")
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
		for(color in CURR_VS_MAP) {
			if(color.value != VS_MAP[color.key]) {
				currentBuild.result = 'ABORTED'
				error("En release ${releaseName}-istio el peso del ${color.key} en Virtual Service ${vsName} deberia ser ser ${VS_MAP[color]} y es ${color.value}")
			}
		}
		if(DOWN_COLOR != "") {
			try {
				(sh(script: "kubectl scale --replicas=0 statefulset/${statefulsetName}-${DOWN_COLOR} -n ${namespace}"))
			} catch(err) {}
		}
	}
}
