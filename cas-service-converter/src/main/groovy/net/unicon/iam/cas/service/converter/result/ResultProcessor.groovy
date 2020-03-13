package net.unicon.iam.cas.service.converter.result

import net.unicon.iam.cas.service.converter.util.CasService
import net.unicon.iam.cas.service.converter.util.ResultFormats


abstract class ResultProcessor {

    private final TreeMap servicesStorage = [:]
    //used for storing services in order to print out results based on evaluation order, value is list of services for that order
    private final Map usernameStorage = [:].withDefault { key -> return [] }
    //key is username to release, value is list of serviceIds that release that username
    private final Map proxyStorage = [:].withDefault { key -> return [] }
    //key is pair of key location and algorithm, value is list of serviceIds that use this key
    private final Map attributeStorage = [:].withDefault { key -> return [] }
    //key is set of release attributes, value is list of serviceIds that release those values (none, default, and all are also possible keys)

    private final resultLocation
    private final resultFormat


    ResultProcessor(resultLocation, resultFormat) {
        this.resultLocation = resultLocation
        this.resultFormat = resultFormat
    }

    abstract void processResults()

    void storeResult(CasService casService) {
        //println "\nProcessing Service # [${remainFileCount}] with id [${casService.id}] with evaluationOrder [${casService.evaluationOrder}]"
        if (servicesStorage.containsKey(casService.evaluationOrder.toInteger())) {
            println "\nWARNING EvaluationOrder [${casService.evaluationOrder.toInteger()}] has duplicate(s)!"
            servicesStorage.get(casService.evaluationOrder.toInteger()).add(casService)
        } else {
            servicesStorage.put(casService.evaluationOrder.toInteger(), [casService])
        }

        if (casService.usernameAttribute && !casService.usernameAttribute.allWhitespace) {
            usernameStorage.get(casService.usernameAttribute).add(casService.serviceId)
        }

        if ((casService?.authorizedToReleaseProxyGrantingTicket == true || casService?.authorizedToReleaseCredentialPassword) && casService?.publicKeyLocation) {
            proxyStorage.get(casService.publicKeyLocation).add(casService.serviceId)
        }

        if (casService.releaseAttributes) {
            attributeStorage.get(casService.releaseAttributes.tokenize(',').toSet().asImmutable()).add((casService.name + casService.id))
        }
    }
}
