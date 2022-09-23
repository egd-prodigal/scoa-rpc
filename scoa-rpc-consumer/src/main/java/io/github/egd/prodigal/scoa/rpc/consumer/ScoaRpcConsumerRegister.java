package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;

public class ScoaRpcConsumerRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableScoaRpcConsumer.class.getName(), true);
        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("basePackages");
            scan(basePackages, registry);
        }
    }

    private void scan(String[] basePackages, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isInterface() && metadata.isIndependent();
            }
        };
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            return classMetadata.isInterface();
        });
        scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);
        for (String basePackage : basePackages) {
            scanner.findCandidateComponents(basePackage).forEach(candidateComponent -> {
                ScannedGenericBeanDefinition beanDefinition = (ScannedGenericBeanDefinition) candidateComponent;
                String value = Objects.requireNonNull(beanDefinition.getBeanClassName());
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(value);
                beanDefinition.setBeanClass(ScoaRpcConsumerFactory.class);
                beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                registry.registerBeanDefinition(beanDefinition.getBeanClass().getName(), beanDefinition);
            });
        }
    }

}
