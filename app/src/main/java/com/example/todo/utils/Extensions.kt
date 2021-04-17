package com.example.todo.utils

inline fun androidx.appcompat.widget.SearchView.setOnTextListener(crossinline listener : (String) -> Unit){

    this.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}
