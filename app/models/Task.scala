package models

import com.ning.http.client.ProgressAsyncHandler

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-24
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
class Task(nameC: String, auto: Boolean) {
  var name: String = nameC
  var progress: Progress = NOT_STARTED
  var isAutomatic: Boolean = auto
}
