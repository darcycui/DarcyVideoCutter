package com.darcy.videocutter.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.darcy.lib_log_toast.exts.logD

/**
 * ViewPager2 适配器
 */
class ViewPager2Adapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    // darcyRefactor: 获取itemId 确保唯一 刷新机制依赖该ID
    override fun getItemId(position: Int): Long {
        val name = fragments[position].hashCode() + position
        val itemID = name.hashCode().toLong()
        logD("getItemId:$itemID")
        return itemID
    }
}