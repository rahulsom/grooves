package grooves.boot.jpa.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.BeanUtils;


/**
 * For variable depth copy, level 0 is just the primitives of the specified object.
 * Each additional level is a copy of the object's non-primitive fields, and so on.
 * 
 * If level is set to zero, non-primitive properties will be set to null and collections will be empty.
 * This copy performs cycle detection and sets any repeated copies to null.
 * 
 * If a property is of type byte[], the copy is a direct reference to the original array, no array copy is done.
 * 
 * This works for Hibernate objects because hibernate's persistent collections aren't copied, 
 * their contents are copied into the default collection of the target class.
 * 
 * Copy is done according to bean properties, not necessarily through field reflection.
 * 
 * This class has no mutable state so is thread-safe.
 */

// TODO look into AOP to summarize returned objects, would need to be done inside the service's transaction though
public class VariableDepthCopier<T>
{

   public T copy(T from) {
      return copy(from, 0);
   }
   public T copy(T from, int depth) {
      T object = (T)createObjectCopy(from, depth);
      return object;
   }
   
   public Object createObjectCopy(Object from, int depth) 
   {
      return createObjectCopy(from, depth, new LinkedList());
   }

   private Object createObjectCopy(Object from, int depth, List ancestors) 
   {
      if(from == null) {
         return null;
      }

      if(ancestors.contains(from)) {
         return null;
      }

      Object copy;
      try {

         copy = BeanUtils.instantiate(from.getClass());

         List<String> nonSimplePropertyNames = getNonSimplePropertyNames(from.getClass());
         BeanUtils.copyProperties(from, copy, nonSimplePropertyNames.toArray(new String[]{}));

         if(depth > 0) {
            ancestors.add(from);
            PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(from.getClass());
            for(PropertyDescriptor pd : descriptors) {

               // copy into collections (target should always have a default empty collection assigned)
               if(Collection.class.isAssignableFrom(pd.getPropertyType())) {
                  Collection sourceCollection = (Collection)pd.getReadMethod().invoke(from);
                  Collection targetCollection = (Collection)pd.getReadMethod().invoke(copy);
                  targetCollection.clear();
                  if (sourceCollection != null) {
                     for(Object j : sourceCollection) {
                        targetCollection.add(createObjectCopy(j, depth - 1, ancestors));
                     }
                  }
                  continue;
               }

               if(nonSimplePropertyNames.contains(pd.getName())) {
                  Object propertyToCopy = pd.getReadMethod().invoke(from);
                  Object propertyCopy   = createObjectCopy(propertyToCopy, depth - 1, ancestors);
                  pd.getWriteMethod().invoke(copy, propertyCopy);
                  continue;
               }
            }

            ancestors.remove(ancestors.size()-1);
         }
      }
      catch(IllegalAccessException | InvocationTargetException ex) {
         throw new UnsupportedOperationException(from.getClass() + " cannot be handled", ex);
      }

      return copy;
   }

   // TODO this could be a @Cacheable method
   private List<String> getNonSimplePropertyNames(Class fromClass) {
      List<String> names = new ArrayList<>();
      for(Field field : fromClass.getDeclaredFields()) {
         if(!BeanUtils.isSimpleProperty(field.getType())) {
            names.add(field.getName());
         }
      }
      return names;
   }
}