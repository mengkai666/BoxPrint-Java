package com.example.cx.boxlabel.infrastructure;

import com.example.cx.boxlabel.domain.ProductTemplateBinding;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class JdbcProductTemplateBindingRepository implements ProductTemplateBindingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductTemplateBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ProductTemplateBinding findByProductConfigId(String productConfigId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT product_config_id, box_template_code, bag_template_code, source " +
                            "FROM LP_PRODUCT_TEMPLATE_BINDING WHERE product_config_id = ?",
                    bindingMapper(),
                    productConfigId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(ProductTemplateBinding binding) {
        if (exists(binding.getProductConfigId())) {
            jdbcTemplate.update(
                    "UPDATE LP_PRODUCT_TEMPLATE_BINDING SET box_template_code = ?, bag_template_code = ?, " +
                            "source = ?, updated_at = CURRENT_TIMESTAMP WHERE product_config_id = ?",
                    binding.getBoxTemplateCode(),
                    binding.getBagTemplateCode(),
                    binding.getSource(),
                    binding.getProductConfigId()
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO LP_PRODUCT_TEMPLATE_BINDING (product_config_id, box_template_code, bag_template_code, source) " +
                            "VALUES (?, ?, ?, ?)",
                    binding.getProductConfigId(),
                    binding.getBoxTemplateCode(),
                    binding.getBagTemplateCode(),
                    binding.getSource()
            );
        }
    }

    private boolean exists(String productConfigId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM LP_PRODUCT_TEMPLATE_BINDING WHERE product_config_id = ?",
                Integer.class,
                productConfigId
        );
        return count != null && count > 0;
    }

    private RowMapper<ProductTemplateBinding> bindingMapper() {
        return new RowMapper<ProductTemplateBinding>() {
            @Override
            public ProductTemplateBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
                ProductTemplateBinding binding = new ProductTemplateBinding();
                binding.setProductConfigId(rs.getString("product_config_id"));
                binding.setBoxTemplateCode(rs.getString("box_template_code"));
                binding.setBagTemplateCode(rs.getString("bag_template_code"));
                binding.setSource(rs.getString("source"));
                return binding;
            }
        };
    }
}
