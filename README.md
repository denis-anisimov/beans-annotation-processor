Beans annotation processor
======
Use it to generate property names for your bean classes.

Setup
======
1. Add dependency to your maven project:
<pre>
<dependency>
	<groupId>den.spb.su</groupId>
	<artifactId>beans-annotation-proc</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>provided</scope>
	<optional>true</optional>
</dependency>
</pre>
1. Configure maven compiler plugin to use annotation processor:
<pre>
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <annotationProcessors>
                    <annotationProcessor>
			su.spb.den.processor.PropertiesAnnotationProcessor
		    </annotationProcessor>
		</annotationProcessors>
	    </configuration>
        </plugin>
    </plugins>
</build>
</pre>


Usage
=========
Annotate your bean class with <code>@BeanProperties</code> annotation.
Run <code>mvn clean compile</code>. A new interface with property names defined as constants will be generated inside <code>target/generated-sources/annotations</code> folder of your project.
If <code>com.example.SomeType</code> is the name of a bean class then the generated interface name is <code>com.example.SomeTypeProperties</code>.
