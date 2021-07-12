export function getApiServerHost() {
    if (process.env.IS_DEBUG == "true") {
        return "127.0.0.1:5001";
        // return "172.16.0.136:5001";
    } else {
        var port = document.location.port ? (":" + document.location.port) : "";
        return document.location.hostname + port;
    }
}