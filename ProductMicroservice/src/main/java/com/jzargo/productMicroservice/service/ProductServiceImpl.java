package com.jzargo.productMicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzargo.core.messages.command.ProductItemCommand;
import com.jzargo.core.messages.command.ProductValidationCommand;
import com.jzargo.core.messages.event.ProductCreatedEvent;
import com.jzargo.core.registry.MessageTypeRegistry;
import com.jzargo.outboxMessaging.outbox.model.Outbox;
import com.jzargo.outboxMessaging.outbox.repository.OutboxRepository;
import com.jzargo.productMicroservice.config.KafkaConfig;
import com.jzargo.productMicroservice.dto.ProductRequest;
import com.jzargo.productMicroservice.exception.ValidateProductException;
import com.jzargo.productMicroservice.mappper.ProductCreateMapper;
import com.jzargo.productMicroservice.model.Product;
import com.jzargo.productMicroservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService{

    private final ProductCreateMapper productCreateMapper;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final MessageTypeRegistry messageTypeRegistry;
    private final OutboxRepository outboxRepository;

    public ProductServiceImpl(ProductCreateMapper productCreateMapper, ProductRepository productRepository, ObjectMapper objectMapper, MessageTypeRegistry messageTypeRegistry, OutboxRepository outboxRepository) {
        this.productCreateMapper = productCreateMapper;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.messageTypeRegistry = messageTypeRegistry;
        this.outboxRepository = outboxRepository;
    }

    @Override
    @Transactional
    public void createProduct(ProductRequest productRequest) throws JsonProcessingException {

        Product product = productCreateMapper.map(productRequest);

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                product.getId(),
                productRequest.getQuantity()
                );

        Outbox build = Outbox.builder()
                .topicName(KafkaConfig.PRODUCT_EVENT_TOPIC)
                .payload(
                        objectMapper.writeValueAsString(productCreatedEvent)
                )
                .key(
                        product.getId().toString()
                )
                .messageType(
                        messageTypeRegistry.getMessageTypeName(productCreatedEvent.getClass())
                )
                .build();

        outboxRepository.save(build);
        productRepository.save(product);

    }

    @Override
    public void validateProduct(ProductValidationCommand cmd)
            throws ValidateProductException{

        List<ProductItemCommand> productItems = cmd.getProductItems();

        if(productItems.isEmpty()) {
            log.error("Product items list is empty for order ID: {}", cmd.getOrderId());
            throw new ValidateProductException();
        }

        BigDecimal total =BigDecimal.ZERO;
        for(ProductItemCommand item: productItems){
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(ValidateProductException::new);
            total = total.add(product.getPrice());
        }

        if(!total.equals( cmd.getTotalPrice())) {
            log.error("Total price mismatch for order ID: {}, expected: {}, actual: {}",
                    cmd.getOrderId(),
                    cmd.getTotalPrice(),
                    total
            );
            throw new ValidateProductException();
        }

    }

}
