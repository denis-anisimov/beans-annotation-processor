Beans annotation processor
======
Use it to generate property names for your bean classes.

Setup
======
1. Add dependency to your maven project:
<pre>
&lt;dependency&gt;
	&lt;groupId&gt;den.spb.su&lt;/groupId&gt;
	&lt;artifactId&gt;beans-annotation-proc&lt;/artifactId&gt;
	&lt;version&gt;1.0-SNAPSHOT&lt;/version&gt;
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


Usage
=========
Annotate your bean class with <code>@BeanProperties</code> annotation.
Run <code>mvn clean compile</code>. A new interface with property names defined as constants will be generated inside <code>target/generated-sources/annotations</code> folder of your project.
If <code>com.example.SomeType</code> is the name of a bean class then the generated interface name is <code>com.example.SomeTypeProperties</code>.
