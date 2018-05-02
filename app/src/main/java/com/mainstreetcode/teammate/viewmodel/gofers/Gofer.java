package com.mainstreetcode.teammate.viewmodel.gofers;

import com.mainstreetcode.teammate.model.ListableModel;

/**
 * Interface for liaisons between a ViewModel and a single instance of it's Model
 */
interface Gofer<T extends ListableModel<T>> {
}
