package models

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-24
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
class Progress extends Enumeration {
  type Progress = Value
  val NOT_STARTED, IN_PROGRESS, SUCCESS, FAILURE = Value
}
