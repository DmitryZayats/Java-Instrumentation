/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package attachtest;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dmitry
 */
public class AttachTest {
    
    public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("Hello World from Java Agent!");
                inst.addTransformer(new MyClassTransformer());
                 if(inst.isRetransformClassesSupported())
                {
                   System.out.println("JVM supports classes retransform!!!\n");
                   inst.addTransformer(new MyClassTransformer());  
                }
                else
                {
                  System.out.println("Sorry, JVM does not support class retransformation\n");
                }
	}
    
     public static void agentmain(String agentArgs, Instrumentation inst) {
		System.out.println("Hello World from Java Agent! Late load...");
                if(inst.isRetransformClassesSupported())
                {
                    try {
                        System.out.println("JVM supports classes retransform!!!\n");
                        inst.addTransformer(new MyClassTransformer(),true);
                        for(Class<?> myClass : inst.getAllLoadedClasses())
                        {
//                          System.out.println("### Loaded class " + myClass.getName() + "\n");
                          if(myClass.getName().equals("forloop.ForLoop"))
                          {
                            System.out.println("Found forloop.ForLoop class");
                            inst.retransformClasses(myClass);
                          }
                        }
                    } catch (UnmodifiableClassException ex) {
                        Logger.getLogger(AttachTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else
                {
                  System.out.println("Sorry, JVM does not support class retransformation\n");
                }
	}
    
     private void showData() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor virtualMachineDescriptor : vms) {
                System.out.println("============ Show JVM: pid = " + virtualMachineDescriptor.id() + " " + virtualMachineDescriptor.displayName());
                VirtualMachine virtualMachine = attach(virtualMachineDescriptor);
                if (virtualMachine != null) {
                    System.out.println("     Java version = " + readSystemProperty(virtualMachine, "java.version"));
        }
     }
    }
    
    private VirtualMachine attach(VirtualMachineDescriptor virtualMachineDescriptor) {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
        } catch (AttachNotSupportedException anse) {
            System.out.println("Couldn't attach " + anse);
        } catch (IOException ioe) {
            System.out.println("Exception attaching or reading a jvm " + ioe);
        } finally {
            //detachSilently(virtualMachine);
        }
        return virtualMachine;
    }
    
    private void detachSilently(VirtualMachine virtualMachine) {
        if (virtualMachine != null) {
            try {
                virtualMachine.detach();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
    
    private String readSystemProperty(VirtualMachine virtualMachine, String propertyName) {
        String propertyValue = null;
        try {
            Properties systemProperties = virtualMachine.getSystemProperties();
            propertyValue = systemProperties.getProperty(propertyName);
        } catch (IOException e) {
            System.out.println("Reading system property failed with error " + e);
        }
        return propertyValue;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        String agentOptions = "listener:true";
        String agentOptions = "script:/home/dmitry/appmain.btm";
        System.out.println("First argument is " + args[0]);
        
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(args[0]);
        } catch (AttachNotSupportedException anse) {
            System.out.println("Couldn't attach " + anse);
        } catch (IOException ioe) {
            System.out.println("Exception attaching or reading a jvm " + ioe);
        }
        try {
              virtualMachine.loadAgent("/home/dmitry/NetBeansProjects/AttachTest/dist/AttachTest.jar", agentOptions);
//            virtualMachine.loadAgent("/var/byteman-download-4.0.1/lib/byteman.jar", agentOptions);
//            virtualMachine.loadAgent("/var/byteman-download-4.0.1/lib/byteman.jar");
//        AttachTest myAttach = new AttachTest();
//        myAttach.showData();
        } catch (AgentLoadException ex) {
            Logger.getLogger(AttachTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AgentInitializationException ex) {
            Logger.getLogger(AttachTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AttachTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException ex) {
//            //Logger.getLogger(myLoop.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
}
