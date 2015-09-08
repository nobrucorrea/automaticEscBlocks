
/**
 * e-Science Central Copyright (C) 2008-2013 School of Computing Science,
 * Newcastle University
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation at: http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, 5th Floor, Boston, MA 02110-1301, USA.
 */
import com.connexience.server.workflow.*;
import org.pipeline.core.data.*;

public class MyService implements WorkflowBlock {
    /**
     * This field refers to property 'Copy Input' defined in service.xml
     */
    private final static String Prop_COPY_INPUT = "Copy Input";

    /**
     * This field refers to input port 'input-1' defined in service.xml
     */
    private final static String Input_INPUT_1 = "input-1";
    
    /**
     * This field refers to output port 'output-1' defined in service.xml
     */
    private final static String Output_OUTPUT_1 = "output-1";


    /**
     * This method is called when block execution is first started. It should be
     * used to setup any data structures that are used throughout the execution
     * lifetime of the block.
     */
    public void preExecute(BlockEnvironment env) throws Exception
    {
        
    }

    /**
     * This code is used to perform the actual block operation. It may be called
     * multiple times if data is being streamed through the block. It is, however, 
     * guaranteed to be called at least once and always after the preExecute
     * method and always before the postExecute method;
     */
    public void execute(BlockEnvironment env, BlockInputs inputs, BlockOutputs outputs) throws Exception
    {
        Data outputData;

        // Check the value of "Copy Input" property
        if (env.getBooleanProperty(Prop_COPY_INPUT, false)) { 
            // Get the data from the input called "input-1"
            Data inputData = inputs.getInputDataSet(Input_INPUT_1);
            
            // Do some data manipulation
            // ...
            
            // Produce output
            outputData = inputData;
        } else {
            outputData = new Data();
        }

        // Pass this to the output called "output-1"
        outputs.setOutputDataSet(Output_OUTPUT_1, outputData);
    }
    
    /*
     * This code is called once when all of the data has passed through the block. 
     * It should be used to cleanup any resources that the block has made use of.
     */
    public void postExecute(BlockEnvironment env) throws Exception
    {
        
    }
}
