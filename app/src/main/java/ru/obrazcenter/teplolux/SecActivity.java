package ru.obrazcenter.teplolux;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
public class SecActivity extends Activity {
    @SuppressWarnings("unused")
    class MyList2 implements List {
        Object[] objects;
        int[] links;
        int mHead;
        int free;
        int size;
        MyList2(int size) {
            objects = new Object[size];
            links = new int[size];
            free = 0;
            int i = 0;
            this.size = size;
            while (i < size-1) {
                links[i] = i+1;
                i++;
            }
            links[size-1] = mHead = -1;
        }

        @Override
        public int size() {
            int h = mHead, i = 0;
            while (h > -1) {
                h = links[h];
                i++; }
            return i;
        }

        @Override
        public boolean add(Object o) {
            if (mHead == -1) {
                objects[free] = o;
                mHead = free;
                free = links[free];
                links[mHead] = -1;
            } else {
                objects[free] = o;
                int theLink = free;
                free = links[free];
                int h = mHead;
                while (links[h] > -1) {
                    h = links[h];
                }
                links[h] = theLink;
                links[theLink] = -1;
            }
            return true;
        }

        public void add2(Object o) {
            objects[free] = o;
            int theLink = free;
            free = links[free];
            int h = mHead;
            while (links[h] > -1) {
                h = links[h];
            }
            links[h] = theLink;
            links[theLink] = -1;
        }

        @Override
        public void add(int index, Object element) {
            if (index < 0 || index >= size())
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            if (index == 0) {
                mHead = free;
                objects[free] = element;
                int h = mHead;
//                for (int i = 0; i < index; i++) {
//                    h = links[h];
//                }
                links[free] =
                        free = links[free];
            } else {

            }
        }

        @Override
        public Object remove(int index) {
            if (index == 0) {
                mHead = links[mHead];

                return objects[mHead];
            } else {
                int h = mHead, i = 0;
                for (; i < index; i++)
                    h = links[h];
                return objects[h];
            }
        }

        @Override
        public boolean remove(Object o) {
        	if (o == null) {
        		for (; ; ) {
        			
        		}
        	}
            return false;
        }

        @Override
        public void clear() {}

        @Override
        public Object get(int index) {
            if (index == 0) return objects[mHead];
            else {
                int h = mHead, i = 0;
                for (; i < index; i++)
                    h = links[h];
                return objects[h];
            }
        }

        @Override
        public Object set(int index, Object element) {
            if (index == 0) return objects[mHead];
            else {
                int h = mHead, i = 0;
                for (; i < index; i++)
                    h = links[h];
                return objects[h];
            }
        }




        @Override
        public int indexOf(Object o) {
            int i = 0, h = mHead;
            if (o != null) {
                for (; i < size; i++) {
                    if (o.equals(objects[h])) return i;
                    h = links[h];
                }
            } else {
                for (; i < size; i++) {
                    if (objects[h] == null) return i;
                    h = links[h];
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            int i = 0, h = mHead;
            int qq = -1;
            if (o != null) {
                for (; i < size; i++) {
                    if (o.equals(objects[h])) qq = i;
                    h = links[h];
                }
            } else {
                for (; i < size; i++) {
                    if (objects[h] == null) qq = i;
                    h = links[h];
                }
            }
            return qq;
        }

        @Override
        public boolean isEmpty() {
            return mHead == -1;
        }






        @Override
        public boolean addAll(@NonNull Collection c) {
            return false;
        }
        @Override
        public boolean addAll(int index, @NonNull Collection c) {
            return false;
        }
        @SuppressWarnings("UnusedAssignment")
        @NonNull
        @Override
        public Object[] toArray() {
            Object[] arr = new Object[size()];
            int h = mHead;
            for (Object o : arr)
                o = objects[h = links[h]];
            return arr;
        }
        @Override
        public boolean contains(Object o) {
            return indexOf(o) > -1;
        }
        @NonNull
        @Override
        public Iterator iterator() {
            return null;
        }
        @Override
        public ListIterator listIterator() {
            return null;
        }
        @NonNull
        @Override
        public ListIterator listIterator(int index) {
            return null;
        }
        @NonNull
        @Override
        public List subList(int fromIndex, int toIndex) {
            return null;
        }
        @Override
        public boolean retainAll(@NonNull Collection c) {
            return false;
        }
        @Override
        public boolean removeAll(@NonNull Collection c) {
            return false;
        }
        @Override
        public boolean containsAll(@NonNull Collection c) {
            return false;
        }
        @NonNull
        @Override
        public Object[] toArray(@NonNull Object[] a) {
            return new Object[0];
        }



        @org.jetbrains.annotations.Contract(pure = true)
        private String outOfBoundsMsg(int index) {
            return "Index: "+index+", Size: "+size;
        }
    }
}