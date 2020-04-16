package hu.dpc.phee.operator.api;

import hu.dpc.phee.operator.audit.BusinessKey;
import hu.dpc.phee.operator.audit.BusinessKeyRepository;
import hu.dpc.phee.operator.audit.Task;
import hu.dpc.phee.operator.audit.TaskRepository;
import hu.dpc.phee.operator.audit.Variable;
import hu.dpc.phee.operator.audit.VariableRepository;
import hu.dpc.phee.operator.business.Transaction;
import hu.dpc.phee.operator.business.TransactionDetail;
import hu.dpc.phee.operator.business.TransactionRepository;
import hu.dpc.phee.operator.business.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RestApiController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BusinessKeyRepository businessKeyRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private VariableRepository variableRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/")
    public void health() {
        // 200 ok
    }

    @GetMapping("/transaction/{workflowInstanceKey}")
    public TransactionDetail transaction(@PathVariable Long workflowInstanceKey) {
        Transaction transaction = transactionRepository.findFirstByWorkflowInstanceKey(workflowInstanceKey);
        List<Task> tasks = taskRepository.findByWorkflowInstanceKeyOrderByTimestamp(workflowInstanceKey);
        List<Variable> variables = variableRepository.findByWorkflowInstanceKeyOrderByTimestamp(workflowInstanceKey);
        return new TransactionDetail(transaction, tasks, variables);
    }

    @GetMapping("/transactions")
    public Page<Transaction> transactions(
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size,
            @RequestParam(value = "payerPartyId", required = false) String payerPartyId,
            @RequestParam(value = "payeePartyId", required = false) String payeePartyId,
            @RequestParam(value = "payeeDfspId", required = false) String payeeDfspId,
            @RequestParam(value = "transactionId", required = false) String transactionId,
            @RequestParam(value = "transactionStatus", required = false) String transactionStatus,
            @RequestParam(value = "amount", required = false) BigDecimal amount,
            @RequestParam(value = "currency", required = false) String currency
    ) {
        Transaction sample = new Transaction();
        sample.setPayerPartyId(payerPartyId);
        sample.setPayeePartyId(payeePartyId);
        sample.setPayeeDfspId(payeeDfspId);
        sample.setTransactionId(transactionId);
        sample.setStatus(parseStatus(transactionStatus));
        sample.setAmount(amount);
        sample.setCurrency(currency);

        return transactionRepository.findAll(Example.of(sample), PageRequest.of(page, size, Sort.by("startedAt").ascending()));
    }

    private TransactionStatus parseStatus(@RequestParam(value = "transactionStatus", required = false) String transactionStatus) {
        try {
            return transactionStatus == null ? null : TransactionStatus.valueOf(transactionStatus);
        } catch (Exception e) {
            logger.warn("failed to parse transaction status {}, ignoring it", transactionStatus);
            return null;
        }
    }

    @GetMapping("/variables")
    public List<List<Variable>> variables(
            @RequestParam(value = "businessKey") String businessKey,
            @RequestParam(value = "businessKeyType") String businessKeyType
    ) {
        return loadTransactions(businessKey, businessKeyType).stream()
                .map(transaction -> variableRepository.findByWorkflowInstanceKeyOrderByTimestamp(transaction.getWorkflowInstanceKey()))
                .collect(Collectors.toList());
    }

    @GetMapping("/tasks")
    public List<List<Task>> tasks(
            @RequestParam(value = "businessKey") String businessKey,
            @RequestParam(value = "businessKeyType") String businessKeyType
    ) {
        return loadTransactions(businessKey, businessKeyType).stream()
                .map(transaction -> taskRepository.findByWorkflowInstanceKeyOrderByTimestamp(transaction.getWorkflowInstanceKey()))
                .collect(Collectors.toList());
    }

    private List<BusinessKey> loadTransactions(@RequestParam("businessKey") String businessKey, @RequestParam("businessKeyType") String businessKeyType) {
        List<BusinessKey> businessKeys = businessKeyRepository.findByBusinessKeyAndBusinessKeyType(businessKey, businessKeyType);
        logger.debug("loaded {} transaction(s) for business key {} of type {}", businessKeys.size(), businessKey, businessKeyType);
        return businessKeys;
    }

}
