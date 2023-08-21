package co.id.middleware.finnet.postgre;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "bankdkiSvc")
public class DataLog extends BaseEntity {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String messageData;

}
