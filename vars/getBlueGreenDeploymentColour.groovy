def call(String statefulsetName, String vsName, String namespace) {
	def BLUE_REPLICAS = 0
	def GREEN_REPLICAS = 0
	def COLOR_PARAM = ""
	def VS_MAP = ["blue": 0,
		"green": 0]
	script {
		try {
            BLUE_REPLICAS = (sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-blue -n ${namespace} -o jsonpath={.status.replicas}")).toInteger()
        } catch(err) {}
        try {
            GREEN_REPLICAS = (sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-green -n ${namespace} -o jsonpath={.status.replicas}")).toInteger()
        } catch(err) {}
		try {
			def VS_JSON = sh(returnStdout: true, script: "kubectl get virtualservice ${vsName} -n ${namespace} -o json")
			def VS_PROPS = readJSON text: VS_JSON
			def VS_ROUTES = VS_PROPS.spec.http[0].route
			for(route in VS_ROUTES) {
				VS_MAP[route.destination.subset] = (route.weight ?: 0).toInteger()
			}
		} catch(err) {
			// no existe deploy de istio, arranco como de cero.
			VS_MAP = ["blue"] = 100
		}
		echo "virtual service map ${VS_MAP.toString()}" 
	}
	script {
		ansiColor('xterm') {
            echo "\033[44m Blue replicas ${BLUE_REPLICAS} Blue virtual service ${VS_MAP['blue']} pct \033[0m"
            echo "\033[42m Green replicas ${GREEN_REPLICAS} Green virtual service ${VS_MAP['green']} pct \033[0m"
            if(BLUE_REPLICAS != 0 && GREEN_REPLICAS == 0 && VS_MAP["blue"] == 100 && VS_MAP["green"] == 0) {
                COLOR_PARAM = 'green'
                echo "\033[42m Deploy Green \033[0m"
            } else if(GREEN_REPLICAS != 0 && BLUE_REPLICAS == 0 && VS_MAP["blue"] == 0 && VS_MAP["green"] == 100) {
                COLOR_PARAM = 'blue'
                echo "\033[44m Deploy Blue \033[0m"
            } else {
                currentBuild.result = 'ABORTED'
                error('No se pudo decidir a que color debería desplegarse.')
            }
        }
		return COLOR_PARAM;
	}
} 
