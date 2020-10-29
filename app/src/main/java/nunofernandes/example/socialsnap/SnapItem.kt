package nunofernandes.example.socialsnap

import nunofernandes.example.socialsnap.helpers.dateToString
import nunofernandes.example.socialsnap.helpers.stringToDate
import java.util.*
import kotlin.collections.HashMap

class SnapItem {

    var filePath    : String?   = null
    var description : String?   = null
    var date        : Date?     = null
    var userID      : String?   = null
    var itemID      : String?   = null

    constructor(
        filePath: String?,
        description: String?,
        date: Date?,
        userID: String
    ) {
        this.filePath       = filePath
        this.description    = description
        this.date           = date
        this.userID         = userID
    }

    fun toHasMap() : HashMap<String, Any?>{
        val hasMap = HashMap<String, Any?>()
        hasMap["filePath"] = filePath
        hasMap["description"] = description
        hasMap["date"] = date?.let { dateToString(it) }
        hasMap["userID"] = userID

        return hasMap
    }

    companion object{
        fun formHash(hashMap:  HashMap<String, Any?>) : SnapItem{
            val item = SnapItem(
                hashMap["filePath"].toString(),
                hashMap["description"].toString(),
                stringToDate(hashMap["date"].toString()),
                hashMap["userID"].toString()
            )
            return item
        }
    }
}