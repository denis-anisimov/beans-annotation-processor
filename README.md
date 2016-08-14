Beans annotation processor & runtime bean property access
======
Use it to generate property names for your bean classes or runtime access to bean properties by their ids.

There are two ways to access bean properties:

 * Generate property names as constants
 * Runtime access to properties via their ids which are attached via annotation values to property accessors.

The setup depends on the way which you prefer to use: 

* If you want to generate property names via annotation processor then there is no need in runtime dependency. In this case dependency is required only at build phase and JAR is not needed in runtime. Use the first way to setup.
* If you want to use property access API (``PropertyAccess``)  then JAR should be included as a runtime dependency.

Setup
======
Annotation processor configuration
-----------
To be able to use annotation processor only use this configuration :

1. Add dependency to your maven project:
<pre>
&lt;dependency&gt;
	&lt;groupId&gt;den.spb.su&lt;/groupId&gt;
	&lt;artifactId&gt;beans-annotation-proc&lt;/artifactId&gt;
	&lt;version&gt;1.1-SNAPSHOT&lt;/version&gt;
	&lt;scope&gt;provided&lt;/scope&gt;
	&lt;optional&gt;true&lt;/optional&gt;
&lt;/dependency&gt;
</pre>
1. Configure maven compiler plugin to use annotation processor:
<pre>
&lt;build&gt;
	&lt;plugins&gt;
		&lt;plugin&gt;
			&lt;artifactId&gt;maven-compiler-plugin&lt;/artifactId&gt;
			&lt;configuration&gt;
				&lt;source&gt;1.8&lt;/source&gt;
				&lt;target&gt;1.8&lt;/target&gt;
				&lt;annotationProcessors&gt;
					&lt;annotationProcessor&gt;
					su.spb.den.processor.PropertiesAnnotationProcessor
					&lt;/annotationProcessor&gt;
				&lt;/annotationProcessors&gt;
			&lt;/configuration&gt;
		&lt;/plugin&gt;
	&lt;/plugins&gt;
&lt;/build&gt;
</pre>

Runtime access configuration
-----------
If you want to use properties access API then you should configure only runtime dependency like this:
<pre>
&lt;dependency&gt;
	&lt;groupId&gt;den.spb.su&lt;/groupId&gt;
	&lt;artifactId&gt;beans-annotation-proc&lt;/artifactId&gt;
	&lt;version&gt;1.1-SNAPSHOT&lt;/version&gt;
	&lt;scope&gt;runtime&lt;/scope&gt;
&lt;/dependency&gt;
</pre>


Usage
=========
Annotation processor usage
-----------

To be able to generate your property names automatically as constants do the following:

* Annotate your bean class with <code>@BeanProperties</code> annotation.
* Run <code>mvn clean compile</code>.
* A new interface with property names defined as constants will be generated inside <code>target/generated-sources/annotations</code> folder of your project.
If <code>com.example.SomeType</code> is the name of a bean class then the generated interface name is <code>com.example.SomeTypeProperties</code>.

The way described above generates only property names for a bean where they are defined without subproperties 
(since they will be generated in another class). To be able to access subproperties you should define your own constants somewhere in the code:

<code>private static final String SUB_PROPERTY = SomeTypeProperties.CONTACT+'.'+ContactProperties.NAME;</code>

It's not convenient sometimes to use autogenerated property names with IDE: if you decide to change property name then IDE will show an error for every occurance changed property in your code. Now you should go through each such error and correct it manually. It's still better than hardcoded property name since you can see each such error and correct the constant name. But e.g. Eclipse sometimes is not able to keep it's functionality for Java class file if there are many such errors. As a result your Java editor becomes uesless untill you fix all such compilation errors. Sometimes it's quite annoying.

To be able to avoid such issues at all you may use runtime property access API

Runtime property access API
-----------

You may access property names on the fly via their ids which are attached to property accessor via <code>@BeanProperty</code>annotation:
 
* Annotate your property getter with <code>@BeanProperty("some-unique-id")</code>.
* In your client code use <code>PropertyAccess.getInstance().getProperty(YouClassName.class,"some-unique-id")</code>
* The result of this class will be the property name derived from the annotated method.

This allows you also get subproperties since you may provide property and subproperties to the <code>PropertyAccess.getProperty()</code> method.
Property/subproperty string is generated on the fly each time when you call the <code>getProperty()</code> method.
This has advantage and disadvantage: 

* The string is reconstructed from scratch each time and it requires some processor's time
* It doesn't take any additional memory.
* It doesn't cache anything.
* It's useful while you are in development since everything works on the fly (especially if you use JRebel). 
* There is no any compilation errors if you decide to rename your property at any time: you don't address the property itself. It's name doesn't matter. You address it via your own defined constant.
* It's much eaiser to do refactoring. 

See the example below.

<code>
public class Contact {

		private Person person;
		private String phone;
     
		@BeanProperty("person-id")
		public Person getPerson(){
			return person;
		} 
      
		@BeanProperty("phone-id")
		public String getPhone(){
			return phone;
		}
}

public class Person {

		private String name;
		private boolean isMale;
     
		@BeanProperty("name-id")
		public String getName(){
			return name;
		} 
		
		@BeanProperty("male-id")
		public boolean isMale(){
			return isMale;
		} 
}

</code>

In your application code you may call the <code>getProperty</code> method:

<code>
String phoneProperty = PropertyAccess.getInstance().getProperty(Contact.class, "phone-id"); // returns "phone";

String nameSubProperty = PropertyAccess.getInstance().getProperty(Contact.class, "person-id","name-id"); // returns "person.name";

String nameProperty = PropertyAccess.getInstance().getProperty(Person.class, "name-id"); // returns "name";

String maleProperty = PropertyAccess.getInstance().getProperty(Person.class, "male-id"); // returns "male";
</code>
