package com.nishant.spring.integration.nodsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.MethodInvokingSplitter;
import org.springframework.integration.store.MessageGroupFactory;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageGroupFactory;
import org.springframework.integration.store.SimpleMessageGroupFactory.GroupType;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.transformer.MethodInvokingTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.fasterxml.jackson.core.JsonProcessingException;
@SpringBootApplication
public class SpringIntegrationClientApplication {

	public static void main(String[] args) throws JsonProcessingException {
		ConfigurableApplicationContext cxt=SpringApplication.run(SpringIntegrationClientApplication.class, args);
		Sender sn=cxt.getBean(Sender.class);
		Scanner scn=new Scanner(System.in);
		while(scn.hasNext()) {
			String msgtosend=scn.next();
			Account act=new Account();
			act.setAccountNo("1234567");
			act.setAccountType(msgtosend);
			AccountHolders acthl1=new AccountHolders();
			acthl1.setName("amit");
			acthl1.setAddress("US");
			AccountHolders acthl2=new AccountHolders();
			acthl2.setName("nishant");
			acthl2.setAddress("UK");
			List<AccountHolders> lst=new ArrayList<>();
			lst.add(acthl1);
			lst.add(acthl2);
			act.setAccountHolders(lst);
			sn.send(act);
		}
		scn.close();
	}

	@MessagingGateway(defaultRequestChannel="messageChannel")
	public interface Sender{
		public void send(Object msg);
	}

	@Bean
	public MessageChannel messageChannel() {
		return new DirectChannel();
	}

	@Splitter(inputChannel="messageChannel")
	@Bean
	public AbstractMessageSplitter router() {
		MethodInvokingSplitter eer=new MethodInvokingSplitter(messageSpl(),"method");
		eer.setOutputChannel(defaultChannel());
		return eer;
	}



	@Bean
	public MessageSpl messageSpl() {
		return new MessageSpl();
	}	

	@Bean
	public MessageChannel defaultChannel() {
		return new DirectChannel();
	}

	@Bean
	@Transformer(inputChannel="defaultChannel")
	public MessageHandler transformer() {
		MessageTransformingHandler sah= new MessageTransformingHandler(new MethodInvokingTransformer(messageSpl(),"transform")) ;
		sah.setOutputChannel(forwrdChannel());
		return sah;
	}

	@Bean
	public MessageChannel forwrdChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageGroupFactory groupFactory() {
		SimpleMessageGroupFactory sms= new SimpleMessageGroupFactory(GroupType.BLOCKING_QUEUE);
		return sms;
	}
	@Bean
	public MessageGroupStore groupStore() {
		SimpleMessageStore sms= new SimpleMessageStore();
		sms.setMessageGroupFactory(groupFactory());
		return sms;
	}

	@ServiceActivator(inputChannel = "forwrdChannel")
	@Bean
	public MessageHandler aggregator(MessageGroupStore messageGroupStore) {
		AggregatingMessageHandler aggregator =
				new AggregatingMessageHandler(new DefaultAggregatingMessageGroupProcessor(),
						messageGroupStore);
		aggregator.setOutputChannel(outputChannel());
		aggregator.setGroupTimeoutExpression(new ValueExpression<>(500L));
		return aggregator;
	}

	@Bean
	public MessageChannel outputChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel="outputChannel")
	public MessageHandler outputChannelhandle() {
		return new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				System.out.println("outputChannel 1 "+message+"<<>>>"+message.getPayload().getClass());
				List<Object> arr=(List<Object>) message.getPayload();
				Account ac = null;
				List<AccountHolders> lah=new ArrayList<>();
				for(Object o:arr) {
					if(o instanceof Account) {
                       ac=(Account) o;
					}else {
						lah.add((AccountHolders) o);
					}
				}
				ac.setAccountHolders(lah);
				System.out.println("outputChannel 2 "+ac.getAccountNo()+"----------"+ac.getAccountHolders().get(0).getName()+"----------"+ac.getAccountHolders().get(1).getName());

			}
		};
	}
}
class MessageSpl{
	public Collection<Object> method(Account msg){
		Collection<Object> col=new ArrayList<>();
		col.add(msg);
		col.addAll(msg.getAccountHolders());
		return col;
	}
	public Object transform(Object msg){
		AccountHolders ah = null;
		Account a = null;
		Object o = null;
		if(msg instanceof AccountHolders) {
			ah=(AccountHolders) msg;
			ah.setName(ah.getName()+"<<<Updated in transformer>>>");
			o=ah;
		}else {
			a=(Account) msg;
			o=a;
		}
		return o;
	}

}
