package models

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-24
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 */
class Phase {
  var name : String
  var tasks: List[Task]
  var nextPhases: List[Phase]

  def addTask(t: Task) = tasks.add(t)
  def addPhase(p: Phase) = nextPhases.add(p)
}
