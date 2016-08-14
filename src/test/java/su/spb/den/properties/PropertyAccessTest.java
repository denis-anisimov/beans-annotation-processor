package su.spb.den.properties;

import java.util.EventObject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author denis
 *
 */
public class PropertyAccessTest {

    private static final String PROPERTY = "property-id";

    public static class SimpleBeanProperties {
        private static final String VALID = "valid-id";
        private static final String NAME = "name-id";
        private static final String ID = "id-id";

        private static final String DUPLICATE = "duplicate-id";
        public static final String NOT_A_PROPERTY = "not-a-property-id";
    }

    public static class SubBeanPropertiesIds {
        private static final String SIMPLE_SUB_BEAN = "simpleBean";
        private static final String SUB_BEAN = "subBean";
    }

    public static class SubBeanProperties {

        private SimpleBean subBean1;

        private SubBeanProperties bean2;

        @BeanProperty(SubBeanPropertiesIds.SIMPLE_SUB_BEAN)
        public SimpleBean getSubBean1() {
            return subBean1;
        }

        public void setSubBean1(SimpleBean subBean1) {
            this.subBean1 = subBean1;
        }

        @BeanProperty(SubBeanPropertiesIds.SUB_BEAN)
        public SubBeanProperties getBean2() {
            return bean2;
        }

        public void setBean2(SubBeanProperties bean2) {
            this.bean2 = bean2;
        }

    }

    public static class ParameterizedBean<T extends SimpleBean> {

        private T subBean;

        @BeanProperty(PROPERTY)
        public T getSubBean() {
            return subBean;
        }

        public void setSubBean(T subBean) {
            this.subBean = subBean;
        }

    }

    public static class GenericBean extends ParameterizedBean<SimpleBean> {

    }

    public static class SimpleBean {
        private boolean isValid;
        private String name;
        private Object id;

        @BeanProperty(SimpleBeanProperties.VALID)
        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean isValid) {
            this.isValid = isValid;
        }

        @BeanProperty(SimpleBeanProperties.NAME)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @BeanProperty(SimpleBeanProperties.ID)
        public boolean hasId() {
            return id != null;
        }

        @BeanProperty(SimpleBeanProperties.DUPLICATE)
        public boolean isVisible() {
            return true;
        }

        @BeanProperty(SimpleBeanProperties.DUPLICATE)
        public Object getId() {
            return id;
        }

        @BeanProperty(SimpleBeanProperties.NOT_A_PROPERTY)
        public void handleEvent(EventObject event) {

        }
    }

    @Test
    public void booleanProperty() {
        Assert.assertEquals(
                "valid",
                PropertyAccess.getInstance().getProperty(SimpleBean.class,
                        SimpleBeanProperties.VALID));
    }

    @Test
    public void simpleStringProperty() {
        Assert.assertEquals(
                "name",
                PropertyAccess.getInstance().getProperty(SimpleBean.class,
                        SimpleBeanProperties.NAME));
    }

    @Test
    public void hasProperty_notJavaBeanSpec() {
        Assert.assertEquals(
                "id",
                PropertyAccess.getInstance().getProperty(SimpleBean.class,
                        SimpleBeanProperties.ID));
    }

    @Test(expected = IllegalStateException.class)
    public void getPropertyByNonUniqueId() {
        PropertyAccess.getInstance().getProperty(SimpleBean.class,
                SimpleBeanProperties.DUPLICATE);
    }

    @Test(expected = IllegalStateException.class)
    public void notMethodForPropertyId() {
        PropertyAccess.getInstance().getProperty(SimpleBean.class, "foo");
    }

    @Test(expected = IllegalStateException.class)
    public void notAPropertyForGivenPropertyId() {
        PropertyAccess.getInstance().getProperty(SimpleBean.class,
                SimpleBeanProperties.NOT_A_PROPERTY);
    }

    @Test(expected = IllegalStateException.class)
    public void emptyPropertyIds() {
        PropertyAccess.getInstance().getProperty(SimpleBean.class);
    }

    @Test
    public void subProperties_simpleBeanProperties() {
        Assert.assertEquals(
                "subBean1",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN));

        Assert.assertEquals(
                "subBean1.name",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN,
                        SimpleBeanProperties.NAME));

        Assert.assertEquals(
                "subBean1.valid",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN,
                        SimpleBeanProperties.VALID));

        Assert.assertEquals(
                "subBean1.id",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN,
                        SimpleBeanProperties.ID));
    }

    @Test
    public void subProperties_recursiveProperties() {
        Assert.assertEquals(
                "bean2",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class, SubBeanPropertiesIds.SUB_BEAN));

        Assert.assertEquals(
                "bean2.subBean1",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class, SubBeanPropertiesIds.SUB_BEAN,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN));

        Assert.assertEquals(
                "bean2.bean2",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class, SubBeanPropertiesIds.SUB_BEAN,
                        SubBeanPropertiesIds.SUB_BEAN));

        Assert.assertEquals(
                "bean2.subBean1.name",
                PropertyAccess.getInstance().getProperty(
                        SubBeanProperties.class, SubBeanPropertiesIds.SUB_BEAN,
                        SubBeanPropertiesIds.SIMPLE_SUB_BEAN,
                        SimpleBeanProperties.NAME));
    }

    @Test
    public void genericBeanProperty() {
        Assert.assertEquals("subBean", PropertyAccess.getInstance()
                .getProperty(GenericBean.class, PROPERTY));
    }

    @Test
    public void genericBeanSubProperty() {
        Assert.assertEquals(
                "subBean.valid",
                PropertyAccess.getInstance().getProperty(GenericBean.class,
                        PROPERTY, SimpleBeanProperties.VALID));
    }

    @Test
    public void rawBeanProperty() {
        Assert.assertEquals("subBean", PropertyAccess.getInstance()
                .getProperty(ParameterizedBean.class, PROPERTY));
    }

    @Test
    public void rawBeanSubProperty() {
        Assert.assertEquals(
                "subBean.name",
                PropertyAccess.getInstance().getProperty(
                        ParameterizedBean.class, PROPERTY,
                        SimpleBeanProperties.NAME));
    }
}
