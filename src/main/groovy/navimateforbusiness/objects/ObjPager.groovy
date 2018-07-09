package navimateforbusiness.objects

import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

/**
 * Created by Siddharth on 09-07-2018.
 */
class ObjPager {
    // ----------------------- Constants ----------------------- //
    // ----------------------- Classes ---------------------------//
    // ----------------------- Interfaces ----------------------- //
    // ----------------------- Globals ----------------------- //
    public int start = 0
    public int count = -1

    // ----------------------- Constructor ----------------------- //
    ObjPager() {}

    ObjPager(int start, int count) {
        this.start = start
        this.count = count
    }

    ObjPager(def json) {
        fromJson(json)
    }

    // ----------------------- Overrides ----------------------- //
    // ----------------------- Public APIs ----------------------- //
    // JSON Converters
    def toJson() {
        return [start: start, count: count]
    }

    void fromJson(def json) {
        this.start = json.start
        this.count = json.count
    }

    // Method to apply current pager on a set of data
    def apply(def list) {
        def pagedList = []

        // Get the index of last object
        int end
        if (count == -1) {
            end = list.size()
        } else {
            end = Math.min(start + count, list.size())
        }

        // Validate count
        if (end - start < 0) {
            throw new ApiException("Invalid pager", Constants.HttpCodes.BAD_REQUEST)
        }

        // Populate array with paged objects
        for (int i = start; i < end; i++) {
            pagedList.push(list[i])
        }

        pagedList
    }

}
