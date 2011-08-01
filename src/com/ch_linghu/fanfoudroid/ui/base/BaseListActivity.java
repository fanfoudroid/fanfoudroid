package com.ch_linghu.fanfoudroid.ui.base;

import android.app.ListActivity;

/**
 * TODO: 准备重构现有的几个ListActivity
 * 
 * 目前几个ListActivity存在的问题是 : 1. 因为要实现[刷新]这些功能, 父类设定了子类继承时必须要实现的方法,
 * 而刷新/获取其实更多的时候可以理解成是ListView的层面, 这在于讨论到底是一个"可刷新的Activity"
 * 还是一个拥有"可刷新的ListView"的Activity, 如果改成后者, 则只要在父类拥有一个实现了可刷新接口的ListView即可,
 * 而无需强制要求子类去直接实现某些方法. 2. 父类过于专制,
 * 比如getLayoutId()等抽象方法的存在只是为了在父类进行setContentView, 而此类方法可以下放到子类去自行实现, 诸如此类的,
 * 应该下放给子类更自由的空间. 理想状态为不使用抽象类. 3. 随着功能扩展, 需要将几个不同的ListActivity子类重复的部分重新抽象到父类来,
 * 已减少代码重复. 4. TwitterList和UserList代码存在重复现象, 可抽象. 5.
 * TwitterList目前过于依赖Cursor类型的List, 而没有Array类型的抽象类.
 * 
 */
public class BaseListActivity extends ListActivity {
}
