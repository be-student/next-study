package http.controller

import http.request.Request
import http.response.Response

interface Controller {

    fun supports(request: Request): Boolean

    fun handle(request: Request): Response
}
