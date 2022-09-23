package io.github.egd.prodigal.scoa.rpc.consumer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoaRpcConsumerRegister implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableScoaRpcConsumer.class.getName(), true);
        String[] basePackages = (String[]) annotationAttributes.get("basePackages");
        scan(basePackages, registry);
    }

    private void scan(String[] basePackages, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                return metadata.isInterface() && metadata.isIndependent();
            }
        };
        scanner.resetFilters(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            return classMetadata.isInterface();
        });
        scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);

        Set<BeanDefinition> candidateComponents = new HashSet<>();
        for (String basePackage : basePackages) {
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }
        candidateComponents.forEach(candidateComponent -> {
            ScannedGenericBeanDefinition beanDefinition = (ScannedGenericBeanDefinition) candidateComponent;
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
            beanDefinition.setBeanClass(ScoaRpcConsumerFactory.class);
            beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            registry.registerBeanDefinition(beanDefinition.getBeanClass().getName(), beanDefinition);
        });
    }

}
