package navimateforbusiness.objects

/**
 * Created by Siddharth on 11-07-2018.
 */
class ObjSorter {
    // ----------------------- Constants ----------------------- //
    // ----------------------- Classes ---------------------------//
    // ----------------------- Interfaces ----------------------- //
    // ----------------------- Globals ----------------------- //
    public def list = []

    // ----------------------- Constructor ----------------------- //
    ObjSorter() {}

    ObjSorter(def list) {
        this.list = list
    }

    // ----------------------- Overrides ----------------------- //
    // ----------------------- Public APIs ----------------------- //
    def replace(String from, String to) {
        // Get index of existing object
        int idx = getIndexOf(from)

        // Replace name if object found
        if (idx != -1) {
            list[idx] = [(to): list[idx][from]]
        }
    }

    def getIndexOf(String name) {
        // Find Sort Object with given name
        def sortObj = list.find {it.keySet()[0].equals(name)}

        if (sortObj) {
            return list.indexOf(sortObj)
        } else {
            return -1
        }
    }

    def getBson() {
        def sorts = [:]

        list.each {sortObj ->
            String key = sortObj.keySet()[0]
            int value = sortObj[key]
            sorts[key] = value
        }

        sorts
    }
}
