def job = hudson.model.Hudson.instance.getItem(args[0])
def buildNbr = job.nextBuildNumber
cause = new hudson.model.Cause.RemoteCause("PipeIt", "This job was orchestrated from PipeIt")
job.scheduleBuild(cause)
def build = job.getBuildByNumber(buildNbr)
while (build == null) {
    sleep(200)
    build = job.getBuildByNumber(buildNbr)
}
def logReader = build.getLogReader()

while (build.isBuilding()) {
    char[] chars = [1024]
    while (logReader.read(chars) > 0) {
        print "${chars}"
    }
    sleep(200)
}
sleep(500)
println "==========================================================================="
println "==========================================================================="
println "${build.getFullDisplayName()}: ${build.state}"
println "${build.getFullDisplayName()}: ${build.getResult()}"
build.getArtifacts().each {art ->
    println "Produced artifact: ${hudson.model.Hudson.getInstance().getRootUrl()}${art.getHref()}"
}
if (build.getResult().isWorseThan(hudson.model.Result.SUCCESS)) {
    println "The build failed!!"
    System.exit(1)
}