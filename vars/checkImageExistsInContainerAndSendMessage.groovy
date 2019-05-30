
def call(String imageName, String tagName, String newRelicProjectNameToShow, String projectName = 'redeo-all') {
    IMAGE_EXISTS = imageExistsInContainerRegistry("${imageName}", "${tagName}")
    if (!IMAGE_EXISTS) {
        def MSG = "Imagen docker gcr.io/${projectName}/${imageName}:${tagName} no encontrada para deploy ${newRelicProjectNameToShow} - prd. No se ha deployado la version indicada. ${BUILD_URL}/console"
        slackSend(color: 'danger', channel: '#test-jenkins', message: MSG, title: "${newRelicProjectNameToShow} - prd")
        currentBuild.result = 'ABORTED'
        error(MSG)
    }
}