package de.simpleworks.staf.framework.elements.composited;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.simpleworks.staf.commons.annotation.Property;
import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.interfaces.ITeststep;
import de.simpleworks.staf.commons.utils.Convert;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool; 
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * @brief Utils Class, that equips Template testcase classes
 */

public class CompositedTestcaseEquipper {

	private static final Logger logger = LogManager.getLogger(CompositedTestcaseEquipper.class);

	protected CompositedTestcaseEquipper() {
		throw new RuntimeException("can't initialize utils class.");
	}

	@SuppressWarnings("deprecation")
	public static void equipClass(final String classpath, List<ITeststep> steps, Map<String, String> properties, Map<String, String> props)
			throws Exception {

		if (Convert.isEmpty(classpath)) {
			throw new IllegalArgumentException("classpath can't be null or empty string.");
		}

		if (Convert.isEmpty(steps)) {
			throw new IllegalArgumentException("steps can't be null or empty.");
		}

		if (Convert.isEmpty(properties)) {
			throw new IllegalArgumentException("properties can't be null or empty.");
		}
		
		if (Convert.isEmpty(props)) {
			throw new IllegalArgumentException("props can't be null or empty.");
		}
		
		ClassPool pool = ClassPool.getDefault();
		CtClass ctClass = pool.get(classpath);

		for (String propertyKey : properties.keySet()) {

			final String propertyValue = properties.getOrDefault(propertyKey, Convert.EMPTY_STRING);
			final String classProperty = String.format("private String %s=\"%s\";", propertyKey, propertyValue);

			CtField f1 = CtField.make(classProperty, ctClass);
			f1.setModifiers(9);
 

			ClassFile classFile = ctClass.getClassFile();
			ConstPool constPool = classFile.getConstPool(); 
 
			Annotation annotation = new Annotation(Property.class.getName(), constPool);
			annotation.addMemberValue("value", new StringMemberValue(props.get(propertyKey), constPool));

			AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
			annotationsAttribute.addAnnotation(annotation);
			f1.getFieldInfo().addAttribute(annotationsAttribute);

			ctClass.addField(f1);
		}

		for (ITeststep step : steps) {

			final String methodName = String.format("step%s", Integer.toString(step.getOrder()));

			CtMethod stepMethod = CtNewMethod.make(equipTestmethod(methodName), ctClass);
			MethodInfo methodInfoGetEid = stepMethod.getMethodInfo();
			ConstPool cp = methodInfoGetEid.getConstPool();

			Annotation annotationNew = new Annotation(Step.class.getName(), cp);

			annotationNew.addMemberValue("manual", new BooleanMemberValue(false, cp));
			annotationNew.addMemberValue("description", new StringMemberValue(step.getName(), cp));
			annotationNew.addMemberValue("order", new IntegerMemberValue(cp, step.getOrder()));

			AnnotationsAttribute attributeNew = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			attributeNew.setAnnotation(annotationNew);

			stepMethod.getMethodInfo().addAttribute(attributeNew);

			ctClass.addMethod(stepMethod);
		}

		// Save class and make it available
		pool.toClass(ctClass, Thread.currentThread().getContextClassLoader(), null);

		showClass(pool.get(classpath));
	}

	private static String equipTestmethod(String name) {

		String result = String.format("public void %s() {\n" + "		//equipped test method\n" + "	}", name);

		return result;
	}

	private static void showClass(CtClass ctClass) throws Exception {

		if (CompositedTestcaseEquipper.logger.isDebugEnabled()) {
			CompositedTestcaseEquipper.logger.debug(String.format("showClass '%s'.", ctClass.getName()));
		}

		// defrost class to make it accessibler
		ctClass.defrost();

		CtField[] fields = ctClass.getFields(); // only non-private fields

		for (CtField field : fields) {

			if (CompositedTestcaseEquipper.logger.isDebugEnabled()) {
				CompositedTestcaseEquipper.logger.debug(String.format("'%s':'%s'", field.getName(), field.getType()));
			}
			 
			
			for (Object annotation : field.getAnnotations()) {

				if (CompositedTestcaseEquipper.logger.isDebugEnabled()) {
					CompositedTestcaseEquipper.logger.debug( annotation);
				}
				
			}
			
		}

		CtMethod[] methods = ctClass.getDeclaredMethods();
		for (CtMethod method : methods) {

			if (CompositedTestcaseEquipper.logger.isDebugEnabled()) {
				CompositedTestcaseEquipper.logger.debug(String.format("method: '%s'.", method.getMethodInfo()));
			}

		}
	}

}