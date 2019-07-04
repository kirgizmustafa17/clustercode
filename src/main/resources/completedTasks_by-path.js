function(doc) {
    if (doc.path) {
        emit(doc.path, doc._id);
    }
}
