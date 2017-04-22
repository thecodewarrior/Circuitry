package thecodewarrior.circuitry

import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.Save
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by TheCodeWarrior
 */
@Savable
class IDStore<T: IdContainer> {
    @Save var id2obj = mutableMapOf<Int, T>()

    fun clear() { id2obj.clear() }

    fun put(id: Int, obj: T) {
        if(id == 0)
            return // id 0 is reserved
        id2obj.put(id, obj)
    }

    fun add(obj: T): Int {
        val r = ThreadLocalRandom.current()
        var id = r.nextInt()
        while(id2obj.containsKey(id) || id == 0) // id 0 is reserved, so the default value of an int is always invalid
            id = r.nextInt()
        obj.id = id
        id2obj.put(id, obj)
        return id
    }

    fun remove(id: Int): T? {
        return id2obj.remove(id)
    }

    operator fun get(id: Int): T? {
        return id2obj.get(id)
    }
}

interface IdContainer { var id: Int }
