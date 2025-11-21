package avito.testtask.domain.models

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val fileUrl: String,
    val localUrl: String?,
    val bookFormat: BookFormat,
    val userId: String,
    val posterImageUrl: String? // ПОНЯТЬ ОТКУДА ЕЕ БРАТЬ
){
    val downloaded: Boolean = !localUrl.isNullOrEmpty()
}

