/**
 * Author: Sarah McCrie (991405606)
 * Pet data model used for storing and displaying pet profiles
 */

package week11.st830661.petpal.data.models

data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val age: Int? = null,
    val photoUrl: String = ""
)
