def call(String statefulsetName, String vsName, String namespace, String clusterName, String projectName = 'redeo-all') {
	def BLUE_REPLICAS = 0
	def GREEN_REPLICAS = 0
	def COLOR_DESC = ""
	withCredentials([file(credentialsId: "jenkins-${projectName}", variable: 'CHECK_PULL_KEYFILE')]) {
		sh """
		gcloud auth activate-service-account --key-file=${CHECK_PULL_KEYFILE} --project ${projectName}
		gcloud container clusters get-credentials ${clusterName} --zone us-east1
		"""
	}
	script {
		try {
            BLUE_REPLICAS = sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-blue -n ${namespace} -o jsonpath={.status.currentReplicas}")
        } catch(err) {}
        try {
            GREEN_REPLICAS = sh(returnStdout: true, script: "kubectl get statefulset ${statefulsetName}-green -n ${namespace} -o jsonpath={.status.currentReplicas}")
        } catch(err) {}
        def VS_JSON = sh(returnStdout: true, script: "kubectl get virtualservice ${vsName} -n ${namespace} -o json")
        // echo VS_JSON.toString()
        def VS_PROPS = readJSON text: VS_JSON
        def VS_ROUTES = VS_PROPS.spec.http[0].route
        // echo BLUE_REPLICAS.toString()
        // echo GREEN_REPLICAS.toString()
        def VS_DESC = ""
        for(route in VS_ROUTES) {
            VS_DESC += " " + route.destination.subset + ":" + route.weight.toString()
        }
        COLOR_DESC = "Replicas blue: ${BLUE_REPLICAS}<br/> Replicas green: ${GREEN_REPLICAS}\n Virtual service: ${VS_DESC}"
	}
	timeout(time: 60, unit: 'SECONDS') {
		script {
			def COLOR_PARAM = input(message: COLOR_DESC, ok: 'Next',
				parameters: [choice(name: 'COLOR', choices: ['blue','green'].join('\n'), description: 'Seleccione color a deployar')])
			return COLOR_PARAM;
		}
	}
} 
