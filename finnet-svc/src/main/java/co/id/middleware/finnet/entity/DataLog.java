package co.id.middleware.finnet.entity;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * @author errykistiyanto@gmail.com 2022-02-17
 */

@Data
@Entity
@Table(name = "history")
public class DataLog extends BaseEntity {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String messageData;
}
