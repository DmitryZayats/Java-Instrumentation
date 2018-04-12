/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attachtest;

import attachtest.metrics.MetricsCollector;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 *
 * @author dmitry
 */
public class MyClassTransformer implements ClassFileTransformer {
  private ClassPool pool;

  public MyClassTransformer() {
	this.pool = ClassPool.getDefault();
  }	

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
	ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
	byte[] modifiedByteCode = classfileBuffer;
        System.out.println("We are in class transformer transform method\n");
        String clazzName = className.replace("/", ".");
        System.out.println("Class name is " + clazzName);
        //Skip all agent classes
        if (clazzName.startsWith("attachtest")) {
                System.out.println("Found class from attachtest package, skipping transformation\n");
                return classfileBuffer;
        }
        //Skip class if it doesn't belong to our Java forloop program
        if (!clazzName.startsWith("forloop")) {
            System.out.println("Class is not from forloop package, skipping transformation\n");
                return classfileBuffer;
	}

//private MetricsCollector collector = new MetricsCollector();
//pool.importPackage("attachtest.metrics");
pool = ClassPool.getDefault();
//Retrieve the class representation i.e. CtClass object
CtClass cclass;
     
      try {
          cclass = pool.get(clazzName);
          //### For loop ###
            for (CtMethod method : cclass.getDeclaredMethods()) 
            {
                try {
                      method.insertBefore("{ System.out.println(\"Hello from transformer:\"); }");
                }
                catch (CannotCompileException ex) 
                    {
                        Logger.getLogger(MyClassTransformer.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
           //### End of for loop
            try {
                return cclass.toBytecode();
            } catch (IOException ex) {
                Logger.getLogger(MyClassTransformer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CannotCompileException ex) {
                Logger.getLogger(MyClassTransformer.class.getName()).log(Level.SEVERE, null, ex);
            } 
      } catch (NotFoundException ex) {
          Logger.getLogger(MyClassTransformer.class.getName()).log(Level.SEVERE, null, ex);
      }
      return classfileBuffer;
    }
}